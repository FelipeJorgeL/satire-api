package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptTracker {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MAX_TRACKED_EMAILS = 10_000;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final ConcurrentMap<String, FailureWindow> failuresByEmail = new ConcurrentHashMap<>();

    public LoginAttemptTracker() {
        this(Clock.systemUTC());
    }

    LoginAttemptTracker(Clock clock) {
        this.clock = clock;
    }

    public synchronized boolean isBlocked(String email) {
        var now = Instant.now(clock);
        removeExpired(now);
        var window = failuresByEmail.get(email);
        if (window == null) {
            return false;
        }
        if (window.expiredAt(now)) {
            return false;
        }
        return window.count() >= MAX_FAILED_ATTEMPTS;
    }

    public synchronized void recordFailure(String email) {
        var now = Instant.now(clock);
        removeExpired(now);
        if (!failuresByEmail.containsKey(email) && failuresByEmail.size() >= MAX_TRACKED_EMAILS) {
            return;
        }
        failuresByEmail.compute(email, (key, current) -> {
            if (current == null || current.expiredAt(now)) {
                return new FailureWindow(1, now);
            }
            return new FailureWindow(current.count() + 1, current.start());
        });
    }

    public synchronized void recordSuccess(String email) {
        failuresByEmail.remove(email);
    }

    private void removeExpired(Instant now) {
        failuresByEmail.entrySet().removeIf(entry -> entry.getValue().expiredAt(now));
    }

    private record FailureWindow(int count, Instant start) {

        boolean expiredAt(Instant now) {
            return start.plus(LOCKOUT_WINDOW).isBefore(now);
        }
    }
}
