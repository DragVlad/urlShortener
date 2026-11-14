package org.shortener.service;

import org.shortener.config.AppConfig;
import org.shortener.dao.ShortLinkDao;
import org.shortener.dao.UserDao;
import org.shortener.model.LinkStatus;
import org.shortener.model.ShortLink;
import org.shortener.model.User;
import org.shortener.util.ShortCodeGenerator;
import org.shortener.util.UrlValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LinkService {

    private final ShortLinkDao shortLinkDao;
    private final UserDao userDao;
    private final ShortCodeGenerator codeGenerator;
    private final UrlValidator urlValidator;
    private final AppConfig appConfig;

    public LinkService(ShortLinkDao shortLinkDao,
                       UserDao userDao,
                       ShortCodeGenerator codeGenerator,
                       UrlValidator urlValidator,
                       AppConfig appConfig) {
        this.shortLinkDao = shortLinkDao;
        this.userDao = userDao;
        this.codeGenerator = codeGenerator;
        this.urlValidator = urlValidator;
        this.appConfig = appConfig;
    }

    public Optional<ShortLink> createLink(UUID ownerId, String originalUrl, int requestedLimit) {
        if (!urlValidator.isValid(originalUrl)) {
            System.out.println("Ошибка: неверный URL.");
            return Optional.empty();
        }

        Optional<User> owner = userDao.findById(ownerId);
        if (owner.isEmpty()) {
            System.out.println("Ошибка: пользователь не найден.");
            return Optional.empty();
        }

        int finalLimit;
        if (requestedLimit <= 0) {
            finalLimit = appConfig.getDefaultVisitLimit();
        } else if (requestedLimit > appConfig.getMaxVisitLimit()) {
            finalLimit = appConfig.getMaxVisitLimit();
        } else {
            finalLimit = requestedLimit;
        }

        String shortCode = codeGenerator.generateUniqueCode();
        Instant createdAt = Instant.now();
        Instant expiresAt = createdAt.plusSeconds(appConfig.getLinkTtlSeconds());

        ShortLink shortLink = new ShortLink(shortCode, originalUrl, ownerId, finalLimit, createdAt, expiresAt);
        shortLinkDao.save(shortLink);

        owner.get().addLinkCode(shortCode);
        userDao.save(owner.get());

        return Optional.of(shortLink);
    }

    public Optional<String> openLink(String shortCode) {
        Optional<ShortLink> linkOptional = shortLinkDao.findByCode(shortCode);
        if (linkOptional.isEmpty()) {
            System.out.println("Ссылка не найдена.");
            return Optional.empty();
        }

        ShortLink shortLink = linkOptional.get();
        Instant now = Instant.now();

        if (shortLink.isExpired(now)) {
            shortLink.setStatus(LinkStatus.EXPIRED);
            shortLinkDao.save(shortLink);
            System.out.println("Ссылка недоступна: истек срок жизни.");
            return Optional.empty();
        }

        if (shortLink.getStatus() == LinkStatus.DELETED) {
            System.out.println("Ссылка удалена.");
            return Optional.empty();
        }

        if (shortLink.isLimitReached()) {
            System.out.println("Ссылка недоступна: исчерпан лимит переходов.");
            return Optional.empty();
        }

        shortLink.incrementVisitCount();
        if (shortLink.isLimitReached()) {
            shortLink.setStatus(LinkStatus.LIMIT_REACHED);
            System.out.println("Внимание: это был последний доступ по ссылке. Лимит исчерпан.");
        }
        shortLinkDao.save(shortLink);

        return Optional.of(shortLink.getOriginalUrl());
    }

    public boolean editLimit(UUID ownerId, String shortCode, int newLimit) {
        Optional<ShortLink> linkOptional = shortLinkDao.findByCode(shortCode);
        if (linkOptional.isEmpty()) {
            System.out.println("Ссылка не найдена.");
            return false;
        }

        ShortLink shortLink = linkOptional.get();

        if (!shortLink.isOwnedBy(ownerId)) {
            System.out.println("Отказано: вы не владелец этой ссылки.");
            return false;
        }

        if (newLimit <= 0) {
            System.out.println("Ошибка: лимит должен быть больше нуля.");
            return false;
        }

        if (newLimit > appConfig.getMaxVisitLimit()) {
            System.out.println("Ошибка: лимит превышает максимум (" + appConfig.getMaxVisitLimit() + ").");
            return false;
        }

        if (newLimit < shortLink.getVisitCount()) {
            System.out.println("Ошибка: лимит не может быть меньше текущего количества переходов (" + shortLink.getVisitCount() + ").");
            return false;
        }

        shortLink.setVisitLimit(newLimit);
        if (shortLink.getStatus() == LinkStatus.LIMIT_REACHED && !shortLink.isLimitReached()) {
            shortLink.setStatus(LinkStatus.ACTIVE);
        }
        shortLinkDao.save(shortLink);
        System.out.println("Лимит обновлён: " + newLimit);
        return true;
    }

    public boolean deleteLink(UUID ownerId, String shortCode) {
        Optional<ShortLink> linkOptional = shortLinkDao.findByCode(shortCode);
        if (linkOptional.isEmpty()) {
            System.out.println("Ссылка не найдена.");
            return false;
        }

        ShortLink shortLink = linkOptional.get();

        if (!shortLink.isOwnedBy(ownerId)) {
            System.out.println("Отказано: вы не владелец этой ссылки.");
            return false;
        }

        shortLink.setStatus(LinkStatus.DELETED);
        shortLinkDao.save(shortLink);

        Optional<User> owner = userDao.findById(ownerId);
        owner.ifPresent(user -> {
            user.removeLinkCode(shortCode);
            userDao.save(user);
        });

        System.out.println("Ссылка удалена.");
        return true;
    }

    public void cleanupExpiredLinks() {
        List<ShortLink> expiredLinks = shortLinkDao.deleteExpired(Instant.now());
        for (ShortLink shortLink : expiredLinks) {
            shortLink.setStatus(LinkStatus.EXPIRED);
            System.out.println("Уведомление: ссылка " + shortLink.getCode() + " удалена по истечении срока жизни.");

            Optional<User> owner = userDao.findById(shortLink.getOwnerId());
            owner.ifPresent(user -> {
                user.removeLinkCode(shortLink.getCode());
                userDao.save(user);
            });
        }
    }
}