package br.com.api.satireapi.domain.customer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class RateLimitKey {

    private RateLimitKey() {
    }

    public static String of(String namespace, String value) {
        var normalized = value == null ? "unknown" : value.trim().toLowerCase();
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(
                (namespace + ":" + normalized).getBytes(StandardCharsets.UTF_8)
            );
            return namespace + ":" + HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
