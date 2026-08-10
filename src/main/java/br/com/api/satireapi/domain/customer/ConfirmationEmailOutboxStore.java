package br.com.api.satireapi.domain.customer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConfirmationEmailOutboxStore {

    void enqueue(UUID customerId, String recipient, String protectedLink);

    Optional<ConfirmationEmailDelivery> claim(UUID outboxId, Instant now);

    List<UUID> findReady(Instant now, int maxAttempts, int limit);

    void resetStale(Instant threshold, Instant now);

    void markSent(UUID outboxId, Instant now);

    void markFailed(UUID outboxId, Instant nextAttemptAt, String sanitizedError);
}
