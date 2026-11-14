package org.shortener.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shortener.dao.impl.InMemoryUserDao;
import org.shortener.model.User;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserDao());
    }

    @Test
    void createsNewUserWhenNullId() {
        User user = userService.getOrCreateUser(null);
        assertNotNull(user.getId());
    }

    @Test
    void createsDifferentUsersOnRepeatedNullCalls() {
        User u1 = userService.getOrCreateUser(null);
        User u2 = userService.getOrCreateUser(null);
        assertNotEquals(u1.getId(), u2.getId());
    }

    @Test
    void returnsExistingUserForKnownId() {
        UUID id = UUID.randomUUID();
        User created = userService.getOrCreateUser(id);
        User again = userService.getOrCreateUser(id);

        assertEquals(created.getId(), again.getId());
        assertSame(created, again);
    }

    @Test
    void restoresUserForUnknownId() {
        UUID id = UUID.randomUUID();
        User user = userService.getOrCreateUser(id);
        assertEquals(id, user.getId());
    }
}