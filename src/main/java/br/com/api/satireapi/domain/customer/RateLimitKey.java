package br.com.api.satireapi.domain.customer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

public final class RateLimitKey {

    private static final int MAX_NAMESPACE_LENGTH = 32;
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9]+(?:-[a-z0-9]+)*");

    private RateLimitKey() {
    }

    public static String of(String namespace, String value) {
        var normalizedNamespace = normalizeNamespace(namespace);
        var normalizedValue = value == null ? "unknown" : value.trim().toLowerCase(Locale.ROOT);
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(
                (normalizedNamespace + "\0" + normalizedValue).getBytes(StandardCharsets.UTF_8)
            );
            return normalizedNamespace + ":"
                + Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String normalizeNamespace(String namespace) {
        if (namespace == null) {
            throw new IllegalArgumentException("Rate limit namespace is required");
        }
        var normalized = namespace.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_NAMESPACE_LENGTH
            || !NAMESPACE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid rate limit namespace");
        }
        return normalized;
    }
}
