package org.shortener.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class BrowserOpenerTest {

  private final BrowserOpener opener = new BrowserOpener();

  @Test
  void doesNotThrowOnBlankInput() {
    assertDoesNotThrow(() -> opener.open(null));
    assertDoesNotThrow(() -> opener.open(""));
    assertDoesNotThrow(() -> opener.open("   "));
  }

  @Test
  void doesNotThrowOnInvalidUrl() {
    assertDoesNotThrow(() -> opener.open("not-a-url"));
  }

  @Test
  void returnsFalseOnInvalidUrl() {
    assertFalse(opener.open("not-a-url"));
  }
}
