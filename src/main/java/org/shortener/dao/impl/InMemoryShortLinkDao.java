package org.shortener.dao.impl;

import org.shortener.dao.ShortLinkDao;
import org.shortener.model.ShortLink;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryShortLinkDao implements ShortLinkDao {

    private final Map<String, ShortLink> linksByCode = new HashMap<>();

    @Override
    public void save(ShortLink shortLink) {
        linksByCode.put(shortLink.getCode(), shortLink);
    }

    @Override
    public Optional<ShortLink> findByCode(String code) {
        return Optional.ofNullable(linksByCode.get(code));
    }

    @Override
    public List<ShortLink> findByOwnerId(UUID ownerId) {
        List<ShortLink> ownedLinks = new ArrayList<>();
        for (ShortLink shortLink : linksByCode.values()) {
            if (shortLink.getOwnerId().equals(ownerId)) {
                ownedLinks.add(shortLink);
            }
        }
        return ownedLinks;
    }

    @Override
    public List<ShortLink> findAll() {
        return new ArrayList<>(linksByCode.values());
    }

    @Override
    public void delete(String code) {
        linksByCode.remove(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return linksByCode.containsKey(code);
    }

    @Override
    public List<ShortLink> deleteExpired(Instant now) {
        List<ShortLink> expiredLinks = new ArrayList<>();
        List<String> codesToDelete = new ArrayList<>();

        for (ShortLink shortLink : linksByCode.values()) {
            if (shortLink.isExpired(now)) {
                expiredLinks.add(shortLink);
                codesToDelete.add(shortLink.getCode());
            }
        }

        for (String code : codesToDelete) {
            linksByCode.remove(code);
        }

        return expiredLinks;
    }
}