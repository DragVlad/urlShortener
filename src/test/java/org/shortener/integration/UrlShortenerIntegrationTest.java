package org.shortener.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shortener.config.AppConfig;
import org.shortener.dao.ShortLinkDao;
import org.shortener.dao.UserDao;
import org.shortener.dao.impl.InMemoryShortLinkDao;
import org.shortener.dao.impl.InMemoryUserDao;
import org.shortener.model.LinkStatus;
import org.shortener.model.ShortLink;
import org.shortener.model.User;
import org.shortener.service.LinkService;
import org.shortener.service.NotificationService;
import org.shortener.service.UserService;
import org.shortener.util.ShortCodeGenerator;
import org.shortener.util.UrlValidator;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortenerIntegrationTest {

    private UserService userService;
    private LinkService linkService;
    private ShortLinkDao shortLinkDao;
    private UserDao userDao;
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        appConfig = AppConfig.load("application.properties");
        userDao = new InMemoryUserDao();
        shortLinkDao = new InMemoryShortLinkDao();
        userService = new UserService(userDao);

        ShortCodeGenerator generator = new ShortCodeGenerator(shortLinkDao::existsByCode,
                appConfig.getCodeLength());
        linkService = new LinkService(shortLinkDao, userDao, generator,
                new UrlValidator(), appConfig, new NotificationService());
    }

    @Test
    void fullFlowCreateOpenEditDelete() {
        // 1. Создаём пользователя
        User user = userService.getOrCreateUser(null);
        assertNotNull(user.getId());

        // 2. Создаём ссылку
        ShortLink created = linkService.createLink(user.getId(),
                "https://example.com/very/long/path", 3).orElseThrow();
        assertNotNull(created.getCode());

        // 3. Открываем — счётчик растёт
        Optional<String> url = linkService.openLink(created.getCode());
        assertTrue(url.isPresent());
        assertEquals("https://example.com/very/long/path", url.get());
        assertEquals(1, shortLinkDao.findByCode(created.getCode()).orElseThrow().getVisitCount());

        // 4. Меняем лимит
        assertTrue(linkService.editLimit(user.getId(), created.getCode(), 10));
        assertEquals(10, shortLinkDao.findByCode(created.getCode()).orElseThrow().getVisitLimit());

        // 5. Удаляем
        assertTrue(linkService.deleteLink(user.getId(), created.getCode()));
        assertEquals(LinkStatus.DELETED,
                shortLinkDao.findByCode(created.getCode()).orElseThrow().getStatus());
        assertTrue(linkService.openLink(created.getCode()).isEmpty());
    }

    @Test
    void twoUsersCannotEditEachOtherLinks() {
        User u1 = userService.getOrCreateUser(null);
        User u2 = userService.getOrCreateUser(null);
        assertNotEquals(u1.getId(), u2.getId());

        ShortLink link = linkService.createLink(u1.getId(), "https://a.example", 5)
                .orElseThrow();

        // u2 не может редактировать/удалять ссылку u1
        assertFalse(linkService.editLimit(u2.getId(), link.getCode(), 20));
        assertFalse(linkService.deleteLink(u2.getId(), link.getCode()));

        // u1 всё ещё владеет и может удалить
        assertTrue(linkService.deleteLink(u1.getId(), link.getCode()));
    }

    @Test
    void limitReachedPreventsFurtherOpens() {
        User user = userService.getOrCreateUser(null);
        ShortLink link = linkService.createLink(user.getId(), "https://example.com", 2)
                .orElseThrow();

        assertTrue(linkService.openLink(link.getCode()).isPresent());
        assertTrue(linkService.openLink(link.getCode()).isPresent());
        assertTrue(linkService.openLink(link.getCode()).isEmpty());

        assertEquals(LinkStatus.LIMIT_REACHED,
                shortLinkDao.findByCode(link.getCode()).orElseThrow().getStatus());
    }

    @Test
    void userLinkCodesStayInSyncWithService() {
        User user = userService.getOrCreateUser(null);
        ShortLink link = linkService.createLink(user.getId(), "https://example.com", 5)
                .orElseThrow();

        assertTrue(userDao.findById(user.getId()).orElseThrow()
                .getLinkCodes().contains(link.getCode()));

        linkService.deleteLink(user.getId(), link.getCode());
        assertFalse(userDao.findById(user.getId()).orElseThrow()
                .getLinkCodes().contains(link.getCode()));
    }

    @Test
    void createLinkWithNonexistentOwnerFails() {
        assertTrue(linkService.createLink(UUID.randomUUID(), "https://example.com", 5)
                .isEmpty());
    }
}