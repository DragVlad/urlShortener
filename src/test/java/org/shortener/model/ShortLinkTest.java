package org.shortener.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ShortLinkTest {

    private ShortLink newLink(int limit, Instant expiresAt) {
        return new ShortLink("code1", "https://example.com", UUID.randomUUID(),
                limit, Instant.now(), expiresAt);
    }

    @Test
    void startsActiveWithZeroVisits() {
        ShortLink link = newLink(5, Instant.now().plusSeconds(60));
        assertEquals(LinkStatus.ACTIVE, link.getStatus());
        assertEquals(0, link.getVisitCount());
        assertEquals(5, link.getVisitLimit());
    }

    @Test
    void isLimitReachedWhenVisitsEqualLimit() {
        ShortLink link = newLink(2, Instant.now().plusSeconds(60));
        assertFalse(link.isLimitReached());
        link.incrementVisitCount();
        assertFalse(link.isLimitReached());
        link.incrementVisitCount();
        assertTrue(link.isLimitReached());
    }

    @Test
    void isExpiredAfterExpiration() {
        ShortLink link = newLink(5, Instant.now().minusSeconds(1));
        assertTrue(link.isExpired(Instant.now()));
    }

    @Test
    void isNotExpiredBeforeExpiration() {
        ShortLink link = newLink(5, Instant.now().plusSeconds(3600));
        assertFalse(link.isExpired(Instant.now()));
    }

    @Test
    void isOwnedByChecksOwnerId() {
        UUID owner = UUID.randomUUID();
        ShortLink link = new ShortLink("c", "https://e.com", owner, 5,
                Instant.now(), Instant.now().plusSeconds(60));
        assertTrue(link.isOwnedBy(owner));
        assertFalse(link.isOwnedBy(UUID.randomUUID()));
    }

    @Test
    void setVisitLimitUpdatesValue() {
        ShortLink link = newLink(5, Instant.now().plusSeconds(60));
        link.setVisitLimit(10);
        assertEquals(10, link.getVisitLimit());
    }

    @Test
    void setStatusUpdatesValue() {
        ShortLink link = newLink(5, Instant.now().plusSeconds(60));
        link.setStatus(LinkStatus.DELETED);
        assertEquals(LinkStatus.DELETED, link.getStatus());
    }
}