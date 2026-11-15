package org.shortener.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  @Test
  void addAndRemoveLinkCode() {
    UUID id = UUID.randomUUID();
    User user = new User(id);

    user.addLinkCode("abc");
    user.addLinkCode("def");
    assertEquals(2, user.getLinkCodes().size());
    assertTrue(user.getLinkCodes().contains("abc"));

    user.removeLinkCode("abc");
    assertEquals(1, user.getLinkCodes().size());
    assertFalse(user.getLinkCodes().contains("abc"));
  }

  @Test
  void removeUnknownCodeIsNoop() {
    User user = new User(UUID.randomUUID());
    user.addLinkCode("abc");
    user.removeLinkCode("zzz");
    assertEquals(1, user.getLinkCodes().size());
  }

  @Test
  void storesId() {
    UUID id = UUID.randomUUID();
    assertEquals(id, new User(id).getId());
  }
}
