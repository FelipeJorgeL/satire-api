package br.com.api.satireapi.infra.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SecureDeploymentConfigurationValidatorTest {

    @TempDir
    Path tempDirectory;

    private Path rootCertificate;

    @BeforeEach
    void createRootCertificate() throws IOException {
        rootCertificate = Files.createFile(tempDirectory.resolve("root.crt"));
    }

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
            secureDatabaseUrl()
        ));
    }

    @Test
    void rejectsHttpBaseUrlInSecureMode() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "http://api.example.com",
                secureDatabaseUrl()
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
                secureDatabaseUrl()
            )
        );
    }

    @Test
    void rejectsNullSecureBaseUrl() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                null,
                secureDatabaseUrl()
            )
        );
    }

    @Test
    void rejectsVerifyFullWithoutExplicitRootCertificate() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "https://api.example.com",
                "jdbc:postgresql://neon.example/neondb?sslmode=verify-full"
            )
        );
    }

    @Test
    void rejectsMissingRootCertificateFile() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "https://api.example.com",
                "jdbc:postgresql://neon.example/neondb?sslmode=verify-full&sslrootcert="
                    + tempDirectory.resolve("missing.crt")
            )
        );
    }

    @Test
    void rejectsMissingPaymentWebhookSecretInSecureMode() {
        assertThrows(IllegalStateException.class, () ->
            SecureDeploymentConfigurationValidator.validate(
                true,
                "https://api.example.com",
                secureDatabaseUrl(),
                ""
            )
        );
    }

    private String secureDatabaseUrl() {
        return "jdbc:postgresql://neon.example/neondb?sslmode=verify-full&sslrootcert="
            + rootCertificate;
    }
}
