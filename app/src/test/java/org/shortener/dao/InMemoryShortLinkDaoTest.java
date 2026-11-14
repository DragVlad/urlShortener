package org.shortener.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shortener.dao.impl.InMemoryShortLinkDao;
import org.shortener.model.ShortLink;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryShortLinkDaoTest {

    private ShortLinkDao dao;

    private ShortLink newLink(String code, UUID owner, Instant expiresAt) {
        return new ShortLink(code, "https://example.com", owner, 10,
                Instant.now(), expiresAt);
    }

    @BeforeEach
    void setUp() {
        dao = new InMemoryShortLinkDao();
    }

    @Test
    void savesAndFindsByCode() {
        ShortLink link = newLink("abc123", UUID.randomUUID(),
                Instant.now().plusSeconds(60));
        dao.save(link);

        var found = dao.findByCode("abc123");
        assertTrue(found.isPresent());
        assertEquals("https://example.com", found.get().getOriginalUrl());
    }

    @Test
    void returnsEmptyForUnknownCode() {
        assertTrue(dao.findByCode("zzz").isEmpty());
    }

    @Test
    void deletesLink() {
        dao.save(newLink("del1", UUID.randomUUID(), Instant.now().plusSeconds(60)));
        dao.delete("del1");
        assertTrue(dao.findByCode("del1").isEmpty());
    }

    @Test
    void existsByCode() {
        UUID owner = UUID.randomUUID();
        assertFalse(dao.existsByCode("x"));
        dao.save(newLink("x", owner, Instant.now().plusSeconds(60)));
        assertTrue(dao.existsByCode("x"));
    }

    @Test
    void findByOwnerIdReturnsOnlyOwnedLinks() {
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();
        dao.save(newLink("a1", owner1, Instant.now().plusSeconds(60)));
        dao.save(newLink("a2", owner1, Instant.now().plusSeconds(60)));
        dao.save(newLink("b1", owner2, Instant.now().plusSeconds(60)));

        List<ShortLink> links = dao.findByOwnerId(owner1);
        assertEquals(2, links.size());
        assertTrue(links.stream().allMatch(l -> l.isOwnedBy(owner1)));
    }

    @Test
    void findAllReturnsEverything() {
        UUID owner = UUID.randomUUID();
        dao.save(newLink("a", owner, Instant.now().plusSeconds(60)));
        dao.save(newLink("b", owner, Instant.now().plusSeconds(60)));
        assertEquals(2, dao.findAll().size());
    }

    @Test
    void deleteExpiredRemovesOnlyExpiredLinks() {
        UUID owner = UUID.randomUUID();
        Instant now = Instant.now();
        dao.save(newLink("exp1", owner, now.minusSeconds(10)));
        dao.save(newLink("exp2", owner, now.minusSeconds(5)));
        dao.save(newLink("act1", owner, now.plusSeconds(100)));

        List<ShortLink> expired = dao.deleteExpired(now);
        assertEquals(2, expired.size());
        assertTrue(dao.findByCode("exp1").isEmpty());
        assertTrue(dao.findByCode("exp2").isEmpty());
        assertTrue(dao.findByCode("act1").isPresent());
    }
}