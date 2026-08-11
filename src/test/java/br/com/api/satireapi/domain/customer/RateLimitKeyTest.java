package br.com.api.satireapi.domain.customer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Test
    void fitsEveryPersistedRegistrationNamespaceWithinTheDatabaseLimit() {
        assertTrue(RateLimitKey.of("registration-ip", "198.51.100.10").length() <= 80);
        assertTrue(RateLimitKey.of("registration-email", "felipe@example.com").length() <= 80);
        assertTrue(RateLimitKey.of("registration-cpf", "12345678901").length() <= 80);
        assertTrue(RateLimitKey.of("email-delivery", "global").length() <= 80);
    }

    @Test
    void rejectsAnInvalidNamespaceInsteadOfPersistingAnUnboundedKey() {
        assertThrows(IllegalArgumentException.class, () -> RateLimitKey.of("x".repeat(33), "value"));
        assertThrows(IllegalArgumentException.class, () -> RateLimitKey.of("invalid namespace", "value"));
    }
}
