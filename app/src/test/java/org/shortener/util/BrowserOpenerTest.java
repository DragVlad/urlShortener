package org.shortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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