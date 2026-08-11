package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import br.com.api.satireapi.domain.customer.RateLimitBucketStore;
import br.com.api.satireapi.domain.customer.RateLimitKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptTracker {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private final RateLimitBucketStore bucketStore;
    private final Clock clock;

    @Autowired
    public LoginAttemptTracker(RateLimitBucketStore bucketStore) {
        this(bucketStore, Clock.systemUTC());
    }

    LoginAttemptTracker(RateLimitBucketStore bucketStore, Clock clock) {
        this.bucketStore = bucketStore;
        this.clock = clock;
    }

    public boolean isBlocked(String email, String origin) {
        var now = Instant.now(clock);
        return bucketStore.isBlocked(key(email, origin), MAX_FAILED_ATTEMPTS, LOCKOUT_WINDOW, now);
    }

    public void recordFailure(String email, String origin) {
        var now = Instant.now(clock);
        bucketStore.recordFailure(key(email, origin), MAX_FAILED_ATTEMPTS, LOCKOUT_WINDOW, now);
    }

    public void recordSuccess(String email, String origin) {
        bucketStore.clear(key(email, origin));
    }

    private static String key(String email, String origin) {
        return RateLimitKey.of("login", email + "|" + origin);
    }
}
