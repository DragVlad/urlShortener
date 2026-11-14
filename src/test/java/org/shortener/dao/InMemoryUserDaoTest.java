package org.shortener.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shortener.dao.impl.InMemoryUserDao;
import org.shortener.model.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserDaoTest {

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        userDao = new InMemoryUserDao();
    }

    @Test
    void savesAndFindsUserById() {
        UUID id = UUID.randomUUID();
        userDao.save(new User(id));

        assertTrue(userDao.findById(id).isPresent());
        assertEquals(id, userDao.findById(id).get().getId());
    }

    @Test
    void returnsEmptyForUnknownId() {
        assertTrue(userDao.findById(UUID.randomUUID()).isEmpty());
    }

    @Test
    void existsById() {
        UUID id = UUID.randomUUID();
        assertFalse(userDao.existsById(id));
        userDao.save(new User(id));
        assertTrue(userDao.existsById(id));
    }

    @Test
    void saveOverwritesSameId() {
        UUID id = UUID.randomUUID();
        userDao.save(new User(id));
        User same = new User(id);
        same.addLinkCode("x");
        userDao.save(same);

        assertEquals(1, userDao.findById(id).orElseThrow().getLinkCodes().size());
    }
}