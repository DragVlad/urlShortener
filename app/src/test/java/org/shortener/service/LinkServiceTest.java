package org.shortener.service;

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
import org.shortener.util.ShortCodeGenerator;
import org.shortener.util.UrlValidator;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkServiceTest {

    private ShortLinkDao shortLinkDao;
    private UserDao userDao;
    private AppConfig appConfig;
    private LinkService linkService;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        appConfig = AppConfig.load("application.properties");
        shortLinkDao = new InMemoryShortLinkDao();
        userDao = new InMemoryUserDao();

        ShortCodeGenerator generator = new ShortCodeGenerator(shortLinkDao::existsByCode,
                appConfig.getCodeLength());
        linkService = new LinkService(shortLinkDao, userDao, generator,
                new UrlValidator(), appConfig, new NotificationService());

        ownerId = UUID.randomUUID();
        userDao.save(new User(ownerId));
    }

    @Test
    void createLinkReturnsLinkAndSavesIt() {
        Optional<ShortLink> result = linkService.createLink(ownerId,
                "https://example.com/page", 5);

        assertTrue(result.isPresent());
        ShortLink link = result.get();
        assertEquals("https://example.com/page", link.getOriginalUrl());
        assertEquals(5, link.getVisitLimit());
        assertEquals(LinkStatus.ACTIVE, link.getStatus());
        assertTrue(shortLinkDao.existsByCode(link.getCode()));
    }

    @Test
    void createLinkFailsForInvalidUrl() {
        Optional<ShortLink> result = linkService.createLink(ownerId, "not-a-url", 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void createLinkFailsForUnknownOwner() {
        Optional<ShortLink> result = linkService.createLink(UUID.randomUUID(),
                "https://example.com", 5);
        assertTrue(result.isEmpty());
    }

    @Test
    void createLinkUsesDefaultLimitWhenNonPositive() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 0)
                .orElseThrow();
        assertEquals(appConfig.getDefaultVisitLimit(), link.getVisitLimit());
    }

    @Test
    void createLinkCapsLimitAtMax() {
        int requested = appConfig.getMaxVisitLimit() + 100;
        ShortLink link = linkService.createLink(ownerId, "https://example.com", requested)
                .orElseThrow();
        assertEquals(appConfig.getMaxVisitLimit(), link.getVisitLimit());
    }

    @Test
    void createLinkRegistersCodeOnOwner() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        User owner = userDao.findById(ownerId).orElseThrow();
        assertTrue(owner.getLinkCodes().contains(link.getCode()));
    }

    @Test
    void openLinkReturnsOriginalUrlAndIncrementsCount() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();

        Optional<String> url = linkService.openLink(link.getCode());
        assertTrue(url.isPresent());
        assertEquals("https://example.com", url.get());
        assertEquals(1, shortLinkDao.findByCode(link.getCode()).orElseThrow().getVisitCount());
    }

    @Test
    void openLinkReturnsEmptyForUnknownCode() {
        assertTrue(linkService.openLink("unknown").isEmpty());
    }

    @Test
    void openLinkReturnsEmptyWhenLimitReachedAndSetsStatus() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 1)
                .orElseThrow();

        assertTrue(linkService.openLink(link.getCode()).isPresent());
        assertTrue(linkService.openLink(link.getCode()).isEmpty());

        ShortLink stored = shortLinkDao.findByCode(link.getCode()).orElseThrow();
        assertEquals(LinkStatus.LIMIT_REACHED, stored.getStatus());
    }

    @Test
    void openLinkReturnsEmptyForExpiredLinkAndSetsStatus() {
        ShortLink expired = new ShortLink("expired1", "https://example.com", ownerId,
                10, Instant.now().minusSeconds(100), Instant.now().minusSeconds(1));
        shortLinkDao.save(expired);

        assertTrue(linkService.openLink("expired1").isEmpty());
        assertEquals(LinkStatus.EXPIRED,
                shortLinkDao.findByCode("expired1").orElseThrow().getStatus());
    }

    @Test
    void openLinkReturnsEmptyForDeletedLink() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        linkService.deleteLink(ownerId, link.getCode());

        assertTrue(linkService.openLink(link.getCode()).isEmpty());
    }

    @Test
    void editLimitUpdatesLimitForOwner() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();

        assertTrue(linkService.editLimit(ownerId, link.getCode(), 20));
        assertEquals(20, shortLinkDao.findByCode(link.getCode()).orElseThrow().getVisitLimit());
    }

    @Test
    void editLimitFailsForNonOwner() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        assertFalse(linkService.editLimit(UUID.randomUUID(), link.getCode(), 20));
        assertEquals(5, shortLinkDao.findByCode(link.getCode()).orElseThrow().getVisitLimit());
    }

    @Test
    void editLimitFailsForUnknownCode() {
        assertFalse(linkService.editLimit(ownerId, "nope", 10));
    }

    @Test
    void editLimitFailsForNonPositiveLimit() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        assertFalse(linkService.editLimit(ownerId, link.getCode(), 0));
        assertFalse(linkService.editLimit(ownerId, link.getCode(), -3));
    }

    @Test
    void editLimitFailsAboveMax() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        assertFalse(linkService.editLimit(ownerId, link.getCode(),
                appConfig.getMaxVisitLimit() + 1));
    }

    @Test
    void editLimitFailsBelowCurrentVisits() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        linkService.openLink(link.getCode());
        linkService.openLink(link.getCode());

        assertFalse(linkService.editLimit(ownerId, link.getCode(), 1));
    }

    @Test
    void editLimitRestoresActiveStatusFromLimitReached() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 1)
                .orElseThrow();
        linkService.openLink(link.getCode());

        assertEquals(LinkStatus.LIMIT_REACHED,
                shortLinkDao.findByCode(link.getCode()).orElseThrow().getStatus());

        assertTrue(linkService.editLimit(ownerId, link.getCode(), 10));
        assertEquals(LinkStatus.ACTIVE,
                shortLinkDao.findByCode(link.getCode()).orElseThrow().getStatus());
    }

    @Test
    void deleteLinkMarksDeletedAndRemovesFromOwner() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();

        assertTrue(linkService.deleteLink(ownerId, link.getCode()));
        assertEquals(LinkStatus.DELETED,
                shortLinkDao.findByCode(link.getCode()).orElseThrow().getStatus());
        assertFalse(userDao.findById(ownerId).orElseThrow()
                .getLinkCodes().contains(link.getCode()));
    }

    @Test
    void deleteLinkFailsForNonOwner() {
        ShortLink link = linkService.createLink(ownerId, "https://example.com", 5)
                .orElseThrow();
        assertFalse(linkService.deleteLink(UUID.randomUUID(), link.getCode()));
        assertEquals(LinkStatus.ACTIVE,
                shortLinkDao.findByCode(link.getCode()).orElseThrow().getStatus());
    }

    @Test
    void deleteLinkFailsForUnknownCode() {
        assertFalse(linkService.deleteLink(ownerId, "nope"));
    }

    @Test
    void cleanupExpiredLinksRemovesExpiredOnesAndFromOwner() {
        ShortLink active = linkService.createLink(ownerId, "https://a.example", 5)
                .orElseThrow();
        ShortLink expired = new ShortLink("exp", "https://b.example", ownerId, 5,
                Instant.now().minusSeconds(100), Instant.now().minusSeconds(1));
        shortLinkDao.save(expired);
        userDao.findById(ownerId).orElseThrow().addLinkCode("exp");

        linkService.cleanupExpiredLinks();

        assertTrue(shortLinkDao.findByCode("exp").isEmpty());
        assertTrue(shortLinkDao.findByCode(active.getCode()).isPresent());
        assertFalse(userDao.findById(ownerId).orElseThrow()
                .getLinkCodes().contains("exp"));
    }

    @Test
    void findLinksByOwnerReturnsOnlyOwnedLinks() {
        linkService.createLink(ownerId, "https://a.example", 5);
        linkService.createLink(ownerId, "https://b.example", 5);

        UUID other = UUID.randomUUID();
        userDao.save(new User(other));
        linkService.createLink(other, "https://c.example", 5);

        List<ShortLink> links = linkService.findLinksByOwner(ownerId);
        assertEquals(2, links.size());
        assertTrue(links.stream().allMatch(l -> l.isOwnedBy(ownerId)));
    }
}
