package br.com.api.satireapi.infra.mail;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class EmailDeliveryRateLimiter {

    private final int maxPerWindow;
    private final Duration window;
    private final Deque<Instant> sentAt = new ArrayDeque<>();

    EmailDeliveryRateLimiter(@Value("${app.mail.outbox.rate-limit-per-minute:30}") int maxPerMinute) {
        this.maxPerWindow = maxPerMinute;
        this.window = Duration.ofMinutes(1);
    }

    synchronized boolean tryAcquire(Instant now) {
        var cutoff = now.minus(window);
        while (!sentAt.isEmpty() && sentAt.peekFirst().isBefore(cutoff)) {
            sentAt.removeFirst();
        }
        if (sentAt.size() >= maxPerWindow) {
            return false;
        }
        sentAt.addLast(now);
        return true;
    }
}
