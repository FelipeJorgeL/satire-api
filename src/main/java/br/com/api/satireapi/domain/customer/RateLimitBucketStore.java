package br.com.api.satireapi.domain.customer;

import java.time.Duration;
import java.time.Instant;

public interface RateLimitBucketStore {

    boolean isBlocked(String key, int maxAttempts, Duration window, Instant now);

    void recordFailure(String key, int maxAttempts, Duration window, Instant now);

    boolean tryAcquire(String key, int maxAttempts, Duration window, Instant now);

    void clear(String key);
}
