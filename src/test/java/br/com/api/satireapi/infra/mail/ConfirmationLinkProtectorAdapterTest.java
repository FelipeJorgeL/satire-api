package br.com.api.satireapi.infra.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class ConfirmationLinkProtectorAdapterTest {

    @Test
    void protectsAndRestoresLinkWithoutStoringPlaintext() {
        var adapter = new ConfirmationLinkProtectorAdapter("test-secret");
        var link = "http://localhost:8080/api/v1/auth/confirm?token=opaque-token";

        var protectedLink = adapter.protect(link);

        assertNotEquals(link, protectedLink);
        assertEquals(link, adapter.unprotect(protectedLink));
    }
}
