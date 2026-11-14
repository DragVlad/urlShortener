package org.shortener.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlValidatorTest {

    private UrlValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlValidator();
    }

    @Test
    void acceptsHttpAndHttps() {
        assertTrue(validator.isValid("http://example.com"));
        assertTrue(validator.isValid("https://example.com/path?q=1"));
    }

    @Test
    void rejectsNullEmptyAndBlank() {
        assertFalse(validator.isValid(null));
        assertFalse(validator.isValid(""));
        assertFalse(validator.isValid("   "));
    }

    @Test
    void rejectsMalformedUrls() {
        assertFalse(validator.isValid("not-a-url"));
        assertFalse(validator.isValid("ftp://example.com"));
        assertFalse(validator.isValid("http://"));
        assertFalse(validator.isValid("https://"));
    }

    @Test
    void acceptsUrlWithoutPath() {
        assertTrue(validator.isValid("https://example.com"));
    }
}