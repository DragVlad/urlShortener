package org.shortener.service;

import org.shortener.dao.UserDao;
import org.shortener.model.User;

import java.util.Optional;
import java.util.UUID;

public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getOrCreateUser(UUID userId) {
        if (userId == null) {
            User newUser = new User(UUID.randomUUID());
            userDao.save(newUser);
            return newUser;
        }

        Optional<User> existingUser = userDao.findById(userId);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User restoredUser = new User(userId);
        userDao.save(restoredUser);
        return restoredUser;
    }
}