package org.shortener.model;

import java.time.Instant;
import java.util.UUID;

public class ShortLink {

    private final String code;
    private final String originalUrl;
    private final UUID ownerId;
    private int visitLimit;
    private int visitCount;
    private final Instant createdAt;
    private final Instant expiresAt;
    private LinkStatus status;

    public ShortLink(String code,
                     String originalUrl,
                     UUID ownerId,
                     int visitLimit,
                     Instant createdAt,
                     Instant expiresAt) {
        this.code = code;
        this.originalUrl = originalUrl;
        this.ownerId = ownerId;
        this.visitLimit = visitLimit;
        this.visitCount = 0;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.status = LinkStatus.ACTIVE;
    }

    public String getCode() {
        return code;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getVisitLimit() {
        return visitLimit;
    }

    public void setVisitLimit(int visitLimit) {
        this.visitLimit = visitLimit;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void incrementVisitCount() {
        this.visitCount++;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public LinkStatus getStatus() {
        return status;
    }

    public void setStatus(LinkStatus status) {
        this.status = status;
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isLimitReached() {
        return visitCount >= visitLimit;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }
}