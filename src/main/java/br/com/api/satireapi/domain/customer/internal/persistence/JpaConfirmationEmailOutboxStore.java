package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.model.ConfirmationEmailOutbox;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailDelivery;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailOutboxStore;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailQueuedEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaConfirmationEmailOutboxStore implements ConfirmationEmailOutboxStore {

    private final ConfirmationEmailOutboxRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    JpaConfirmationEmailOutboxStore(
        ConfirmationEmailOutboxRepository repository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void enqueue(UUID customerId, String recipient, String protectedLink) {
        var outbox = ConfirmationEmailOutbox.queue(customerId, recipient, protectedLink, Instant.now());
        repository.save(outbox);
        eventPublisher.publishEvent(new ConfirmationEmailQueuedEvent(outbox.getId()));
    }

    @Override
    @Transactional
    public Optional<ConfirmationEmailDelivery> claim(UUID outboxId, Instant now) {
        var claimed = repository.claim(
            outboxId,
            now,
            ConfirmationEmailOutbox.Status.PENDING,
            ConfirmationEmailOutbox.Status.FAILED,
            ConfirmationEmailOutbox.Status.SENDING
        );
        if (claimed != 1) {
            return Optional.empty();
        }
        return repository.findById(outboxId)
            .map(outbox -> new ConfirmationEmailDelivery(
                outbox.getId(),
                outbox.getRecipient(),
                outbox.getProtectedLink(),
                outbox.getAttempts()
            ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findReady(Instant now, int maxAttempts, int limit) {
        return repository.findReady(
            now,
            maxAttempts,
            ConfirmationEmailOutbox.Status.PENDING,
            ConfirmationEmailOutbox.Status.FAILED,
            PageRequest.of(0, limit)
        );
    }

    @Override
    @Transactional
    public void resetStale(Instant threshold, Instant now) {
        repository.resetStale(
            threshold,
            now,
            ConfirmationEmailOutbox.Status.PENDING,
            ConfirmationEmailOutbox.Status.SENDING
        );
    }

    @Override
    @Transactional
    public void markSent(UUID outboxId, Instant now) {
        repository.markSent(
            outboxId,
            now,
            ConfirmationEmailOutbox.Status.SENT,
            ConfirmationEmailOutbox.Status.SENDING
        );
    }

    @Override
    @Transactional
    public void markFailed(UUID outboxId, Instant nextAttemptAt, String sanitizedError) {
        repository.markFailed(
            outboxId,
            nextAttemptAt,
            sanitizedError,
            Instant.now(),
            ConfirmationEmailOutbox.Status.FAILED,
            ConfirmationEmailOutbox.Status.SENDING
        );
    }
}
