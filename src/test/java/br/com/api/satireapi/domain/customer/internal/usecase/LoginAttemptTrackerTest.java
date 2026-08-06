package br.com.api.satireapi.domain.customer.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class LoginAttemptTrackerTest {

    private static final String EMAIL = "felipe@example.com";
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");

    private final Clock clock = mock(Clock.class);
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(clock);

    @Test
    void blocksAfterFiveFailuresInsideWindow() {
        when(clock.instant()).thenReturn(START);

        for (int i = 0; i < 4; i++) {
            tracker.recordFailure(EMAIL);
        }
        assertFalse(tracker.isBlocked(EMAIL));

        tracker.recordFailure(EMAIL);
        assertTrue(tracker.isBlocked(EMAIL));
    }

    @Test
    void unblocksAfterWindowExpires() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL);
        }
        assertTrue(tracker.isBlocked(EMAIL));

        when(clock.instant()).thenReturn(START.plusSeconds(16 * 60));
        assertFalse(tracker.isBlocked(EMAIL));
    }

    @Test
    void successResetsFailureCount() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL);
        }
        assertTrue(tracker.isBlocked(EMAIL));

        tracker.recordSuccess(EMAIL);
        assertFalse(tracker.isBlocked(EMAIL));
    }

    @Test
    void tracksEmailsIndependently() {
        when(clock.instant()).thenReturn(START);
        for (int i = 0; i < 5; i++) {
            tracker.recordFailure(EMAIL);
        }

        assertTrue(tracker.isBlocked(EMAIL));
        assertFalse(tracker.isBlocked("outro@example.com"));
    }
}
