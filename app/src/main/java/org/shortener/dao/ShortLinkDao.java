package org.shortener.dao;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.shortener.model.ShortLink;

public interface ShortLinkDao {

  void save(ShortLink shortLink);

  Optional<ShortLink> findByCode(String code);

  List<ShortLink> findByOwnerId(UUID ownerId);

  List<ShortLink> findAll();

  void delete(String code);

  boolean existsByCode(String code);

  List<ShortLink> deleteExpired(Instant now);
}
