package br.com.api.satireapi.infra.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SecureDeploymentConfigurationValidatorTest {

    private static final String SECURE_DATABASE_URL =
        "jdbc:postgresql://neon.example/neondb?sslmode=verify-full";

    @Test
    void allowsLocalHttpAndDatabaseWithoutTlsWhenSecureModeIsDisabled() {
        assertDoesNotThrow(() -> SecureDeploymentConfigurationValidator.validate(
            false,
            "http://localhost:8080",
            "jdbc:postgresql://localhost:5432/satire"
        ));
    }

    @Test
    void acceptsHttpsAndVerifyFullDatabaseTlsInSecureMode() {
        assertDoesNotThrow(() -> SecureDeploymentConfigurationValidator.validate(
            true,
            "https://api.example.com",
            SECURE_DATABASE_URL
        ));
    }

    @Test
    void rejectsHttpBaseUrlInSecureMode() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "http://api.example.com",
                SECURE_DATABASE_URL
            )
        );
    }

    @Test
    void rejectsDatabaseWithoutVerifyFullTlsInSecureMode() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "https://api.example.com",
                "jdbc:postgresql://neon.example/neondb?sslmode=require"
            )
        );
    }

    @Test
    void rejectsMalformedSecureBaseUrl() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "not-a-url",
                SECURE_DATABASE_URL
            )
        );
    }

    @Test
    void rejectsNullSecureBaseUrl() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                null,
                SECURE_DATABASE_URL
            )
        );
    }
}
