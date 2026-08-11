package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import br.com.api.satireapi.domain.customer.RateLimitBucketStore;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoginAttemptTrackerTest {

    private static final String EMAIL = "felipe@example.com";
    private static final String ORIGIN_A = "198.51.100.10";
    private static final String ORIGIN_B = "198.51.100.11";
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    private final Clock clock = mock(Clock.class);
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(new FakeBucketStore(), clock);

    @Test
    void blocksAfterFiveFailuresInsideWindow() {
        when(clock.instant()).thenReturn(START);

        for (int i = 0; i < 4; i++) {
            tracker.recordFailure(EMAIL, ORIGIN_A);
        }
        assertFalse(tracker.isBlocked(EMAIL, ORIGIN_A));

        tracker.recordFailure(EMAIL, ORIGIN_A);
        assertTrue(tracker.isBlocked(EMAIL, ORIGIN_A));
    }

    @Test
    void unblocksAfterWindowExpires() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL, ORIGIN_A);
        }
        assertTrue(tracker.isBlocked(EMAIL, ORIGIN_A));

        when(clock.instant()).thenReturn(START.plusSeconds(16 * 60));
        assertFalse(tracker.isBlocked(EMAIL, ORIGIN_A));
    }

    @Test
    void successResetsFailureCount() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL, ORIGIN_A);
        }
        assertTrue(tracker.isBlocked(EMAIL, ORIGIN_A));

        tracker.recordSuccess(EMAIL, ORIGIN_A);
        assertFalse(tracker.isBlocked(EMAIL, ORIGIN_A));
    }

    @Test
    void tracksEmailsIndependently() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL, ORIGIN_A);
        }

        assertTrue(tracker.isBlocked(EMAIL, ORIGIN_A));
        assertFalse(tracker.isBlocked("outro@example.com", ORIGIN_A));
    }

    @Test
    void sameEmailFailuresAreScopedToOrigin() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL, ORIGIN_A);
        }

        assertTrue(tracker.isBlocked(EMAIL, ORIGIN_A));
        assertFalse(tracker.isBlocked(EMAIL, ORIGIN_B));
    }

    private static final class FakeBucketStore implements RateLimitBucketStore {

        private final Map<String, Bucket> buckets = new HashMap<>();

        @Override
        public boolean isBlocked(String key, int maxAttempts, Duration window, Instant now) {
            var bucket = buckets.get(key);
            return bucket != null
                && bucket.start().plus(window).isAfter(now)
                && bucket.attempts() >= maxAttempts;
        }

        @Override
        public void recordFailure(String key, int maxAttempts, Duration window, Instant now) {
            var current = buckets.get(key);
            if (current == null || !current.start().plus(window).isAfter(now)) {
                buckets.put(key, new Bucket(now, 1));
                return;
            }
            buckets.put(key, new Bucket(current.start(), Math.min(maxAttempts, current.attempts() + 1)));
        }

        @Override
        public boolean tryAcquire(String key, int maxAttempts, Duration window, Instant now) {
            var current = buckets.get(key);
            if (current != null && current.start().plus(window).isAfter(now)
                && current.attempts() >= maxAttempts) {
                return false;
            }
            recordFailure(key, maxAttempts, window, now);
            return true;
        }

        @Override
        public void clear(String key) {
            buckets.remove(key);
        }

        private record Bucket(Instant start, int attempts) {
        }
    }
}
