package br.com.api.satireapi.infra.config;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SecureDeploymentConfigurationValidator {

    private final boolean secureTransportRequired;
    private final String baseUrl;
    private final String databaseUrl;
    private final String paymentWebhookSecret;

    SecureDeploymentConfigurationValidator(
        @Value("${app.security.require-secure-transport:false}") boolean secureTransportRequired,
        @Value("${app.base-url}") String baseUrl,
        @Value("${spring.datasource.url}") String databaseUrl,
        @Value("${app.payment.webhook.secret:}") String paymentWebhookSecret
    ) {
        this.secureTransportRequired = secureTransportRequired;
        this.baseUrl = baseUrl;
        this.databaseUrl = databaseUrl;
        this.paymentWebhookSecret = paymentWebhookSecret;
    }

    @PostConstruct
    void validateOnStartup() {
        validate(secureTransportRequired, baseUrl, databaseUrl, paymentWebhookSecret);
    }

    static void validate(boolean secureTransportRequired, String baseUrl, String databaseUrl) {
        validate(
            secureTransportRequired,
            baseUrl,
            databaseUrl,
            "test-webhook-secret-with-at-least-32-bytes"
        );
    }

    static void validate(
        boolean secureTransportRequired,
        String baseUrl,
        String databaseUrl,
        String paymentWebhookSecret
    ) {
        if (!secureTransportRequired) {
            return;
        }
        validateHttpsBaseUrl(baseUrl);
        validatePostgresTls(databaseUrl);
        validateWebhookSecret(paymentWebhookSecret);
    }

    private static void validateHttpsBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            throw new IllegalStateException("APP_BASE_URL must be a valid HTTPS URL in secure mode");
        }
        try {
            var uri = URI.create(baseUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
                throw new IllegalStateException("APP_BASE_URL must use HTTPS in secure mode");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("APP_BASE_URL must be a valid HTTPS URL in secure mode", exception);
        }
    }

    private static void validatePostgresTls(String databaseUrl) {
        if (databaseUrl == null || !databaseUrl.startsWith("jdbc:postgresql://")) {
            throw new IllegalStateException("DB_URL must be a PostgreSQL JDBC URL in secure mode");
        }
        var queryStart = databaseUrl.indexOf('?');
        if (queryStart < 0 || queryStart == databaseUrl.length() - 1) {
            throw new IllegalStateException("DB_URL must require sslmode=verify-full in secure mode");
        }
        var parameters = Arrays.stream(databaseUrl.substring(queryStart + 1).split("&"))
            .map(parameter -> parameter.split("=", 2))
            .filter(parameter -> parameter.length == 2)
            .collect(Collectors.toMap(
                parameter -> parameter[0],
                parameter -> parameter[1],
                (first, second) -> second
            ));
        if (!"verify-full".equalsIgnoreCase(parameters.get("sslmode"))) {
            throw new IllegalStateException("DB_URL must require sslmode=verify-full in secure mode");
        }
        validateRootCertificate(parameters.get("sslrootcert"));
    }

    private static void validateWebhookSecret(String secret) {
        if (secret == null || secret.isBlank()
            || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                "PAYMENT_WEBHOOK_SECRET must contain at least 32 bytes in secure mode"
            );
        }
    }

    private static void validateRootCertificate(String configuredPath) {
        if (configuredPath == null || configuredPath.isBlank()) {
            throw new IllegalStateException("DB_URL must define sslrootcert in secure mode");
        }
        try {
            var decodedPath = URLDecoder.decode(configuredPath, StandardCharsets.UTF_8);
            var rootCertificate = Path.of(decodedPath);
            if (!rootCertificate.isAbsolute() || !Files.isRegularFile(rootCertificate)) {
                throw new IllegalStateException(
                    "DB_URL sslrootcert must point to an existing absolute file in secure mode"
                );
            }
        } catch (InvalidPathException exception) {
            throw new IllegalStateException("DB_URL sslrootcert path is invalid", exception);
        }
    }
}
