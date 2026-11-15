package org.shortener.dao;

import java.util.Optional;
import java.util.UUID;
import org.shortener.model.User;

public interface UserDao {

  void save(User user);

  Optional<User> findById(UUID userId);

  boolean existsById(UUID userId);
}
