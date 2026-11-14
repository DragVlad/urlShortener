package org.shortener.dao.impl;

import org.shortener.dao.UserDao;
import org.shortener.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryUserDao implements UserDao {

    private final Map<UUID, User> usersById = new HashMap<>();

    @Override
    public void save(User user) {
        usersById.put(user.getId(), user);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public boolean existsById(UUID userId) {
        return usersById.containsKey(userId);
    }
}