package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import br.com.api.satireapi.domain.customer.RateLimitBucketStore;
import br.com.api.satireapi.domain.customer.RateLimitKey;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class RegistrationRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final RateLimitBucketStore bucketStore;

    public RegistrationRateLimiter(RateLimitBucketStore bucketStore) {
        this.bucketStore = bucketStore;
    }

    public boolean allow(String ipAddress, String email, String cpf) {
        var now = Instant.now();
        return bucketStore.tryAcquire(RateLimitKey.of("registration-ip", ipAddress), MAX_ATTEMPTS, WINDOW, now)
            && bucketStore.tryAcquire(RateLimitKey.of("registration-email", email), MAX_ATTEMPTS, WINDOW, now)
            && (cpf == null || cpf.isBlank()
                || bucketStore.tryAcquire(RateLimitKey.of("registration-cpf", cpf), MAX_ATTEMPTS, WINDOW, now));
    }
}
