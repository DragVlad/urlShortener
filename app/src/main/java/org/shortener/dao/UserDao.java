package org.shortener.dao;

import org.shortener.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserDao {

    void save(User user);

    Optional<User> findById(UUID userId);

    boolean existsById(UUID userId);
}