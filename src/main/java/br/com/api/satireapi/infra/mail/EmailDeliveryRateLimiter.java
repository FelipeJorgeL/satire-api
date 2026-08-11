package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.RateLimitBucketStore;
import br.com.api.satireapi.domain.customer.RateLimitKey;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class EmailDeliveryRateLimiter {

    private final int maxPerWindow;
    private final Duration window = Duration.ofMinutes(1);
    private final RateLimitBucketStore bucketStore;

    EmailDeliveryRateLimiter(
        RateLimitBucketStore bucketStore,
        @Value("${app.mail.outbox.rate-limit-per-minute:30}") int maxPerMinute
    ) {
        this.bucketStore = bucketStore;
        this.maxPerWindow = Math.max(1, maxPerMinute);
    }

    boolean tryAcquire(Instant now) {
        return bucketStore.tryAcquire(
            RateLimitKey.of("email-delivery", "global"), maxPerWindow, window, now
        );
    }
}
