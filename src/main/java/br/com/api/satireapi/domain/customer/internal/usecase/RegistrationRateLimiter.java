package br.com.api.satireapi.domain.customer.internal.usecase;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RegistrationRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String ipAddress, String email, String cpf) {
        var now = Instant.now();
        return allowed(key("ip", ipAddress), now)
            && allowed(key("email", email), now)
            && (cpf == null || cpf.isBlank() || allowed(key("cpf", cpf), now));
    }

    private boolean allowed(String key, Instant now) {
        var window = windows.computeIfAbsent(key, ignored -> new Window());
        synchronized (window) {
            var cutoff = now.minus(WINDOW);
            while (!window.attempts.isEmpty() && window.attempts.peekFirst().isBefore(cutoff)) {
                window.attempts.removeFirst();
            }
            if (window.attempts.size() >= MAX_ATTEMPTS) {
                return false;
            }
            window.attempts.addLast(now);
            return true;
        }
    }

    private static String key(String type, String value) {
        var normalized = value == null ? "unknown" : value.trim().toLowerCase();
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(
                (type + ":" + normalized).getBytes(StandardCharsets.UTF_8)
            );
            return type + ":" + java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private static final class Window {
        private final Deque<Instant> attempts = new ArrayDeque<>();
    }
}
