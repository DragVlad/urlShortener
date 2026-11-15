package org.shortener.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.shortener.model.ShortLink;

class NotificationServiceTest {

  private final NotificationService ns = new NotificationService();

  private ShortLink link() {
    return new ShortLink(
        "abc",
        "https://example.com",
        UUID.randomUUID(),
        5,
        Instant.now(),
        Instant.now().plusSeconds(60));
  }

  @Test
  void allNotifyMethodsDoNotThrow() {
    assertDoesNotThrow(() -> ns.notifyLimitReached(link()));
    assertDoesNotThrow(() -> ns.notifyExpired(link()));
    assertDoesNotThrow(() -> ns.notifyAccessDenied("reason"));
    assertDoesNotThrow(() -> ns.notifyError("message"));
    assertDoesNotThrow(() -> ns.notifyInfo("info"));
  }
}
