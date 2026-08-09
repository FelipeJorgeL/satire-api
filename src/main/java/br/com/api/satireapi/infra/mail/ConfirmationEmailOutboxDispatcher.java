package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailOutboxStore;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailQueuedEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Semaphore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class ConfirmationEmailOutboxDispatcher {

    private static final int BATCH_SIZE = 100;

    private final ConfirmationEmailOutboxStore outboxStore;
    private final ConfirmationEmailSenderAdapter delivery;
    private final Duration staleAfter;
    private final int maxAttempts;
    private final Semaphore bulkhead;

    ConfirmationEmailOutboxDispatcher(
        ConfirmationEmailOutboxStore outboxStore,
        ConfirmationEmailSenderAdapter delivery,
        @Value("${app.mail.outbox.stale-after:PT15M}") Duration staleAfter,
        @Value("${app.mail.outbox.max-attempts:5}") int maxAttempts,
        @Value("${app.mail.outbox.bulkhead:4}") int bulkheadSize
    ) {
        this.outboxStore = outboxStore;
        this.delivery = delivery;
        this.staleAfter = staleAfter;
        this.maxAttempts = maxAttempts;
        this.bulkhead = new Semaphore(bulkheadSize);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmailQueued(ConfirmationEmailQueuedEvent event) {
        dispatch(event.outboxId());
    }

    @Scheduled(fixedDelayString = "${app.mail.outbox.poll-interval:PT1M}")
    public void retryPendingEmails() {
        var now = Instant.now();
        outboxStore.resetStale(now.minus(staleAfter), now);
        outboxStore.findReady(now, maxAttempts, BATCH_SIZE).forEach(this::dispatch);
    }

    private void dispatch(java.util.UUID outboxId) {
        if (!bulkhead.tryAcquire()) {
            return;
        }
        try {
            delivery.dispatch(outboxId);
        } finally {
            bulkhead.release();
        }
    }
}
