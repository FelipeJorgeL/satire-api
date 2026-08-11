package br.com.api.satireapi.domain.customer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RateLimitKeyTest {

    @Test
    void doesNotPersistTheRawIdentifier() {
        var key = RateLimitKey.of("login", "felipe@example.com|198.51.100.10");

        assertTrue(key.startsWith("login:"));
        assertFalse(key.contains("felipe@example.com"));
        assertFalse(key.contains("198.51.100.10"));
        assertNotEquals(key, RateLimitKey.of("login", "another@example.com|198.51.100.10"));
    }
}
