package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.ConfirmationEmailOutboxStore;
import br.com.api.satireapi.domain.customer.ConfirmationEmailSender;
import br.com.api.satireapi.domain.customer.ConfirmationLinkProtector;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ConfirmationEmailSenderAdapter {

    private static final Pattern URL = Pattern.compile("(?i)https?://\\S+");
    private static final int MAX_ERROR_LENGTH = 500;

    private final ConfirmationEmailOutboxStore outboxStore;
    private final ConfirmationEmailSender emailSender;
    private final ConfirmationLinkProtector linkProtector;
    private final EmailDeliveryRateLimiter rateLimiter;
    private final long retryBaseSeconds;

    ConfirmationEmailSenderAdapter(
        ConfirmationEmailOutboxStore outboxStore,
        ConfirmationEmailSender emailSender,
        ConfirmationLinkProtector linkProtector,
        EmailDeliveryRateLimiter rateLimiter,
        @Value("${app.mail.outbox.retry-base:PT30S}") java.time.Duration retryBase
    ) {
        this.outboxStore = outboxStore;
        this.emailSender = emailSender;
        this.linkProtector = linkProtector;
        this.rateLimiter = rateLimiter;
        this.retryBaseSeconds = Math.max(1, retryBase.toSeconds());
    }

    void dispatch(UUID outboxId) {
        var now = Instant.now();
        if (!rateLimiter.tryAcquire(now)) {
            return;
        }
        var delivery = outboxStore.claim(outboxId, now);
        if (delivery.isEmpty()) {
            return;
        }
        try {
            var link = linkProtector.unprotect(delivery.get().protectedLink());
            emailSender.sendEmailConfirmation(delivery.get().recipient(), link);
            outboxStore.markSent(outboxId, Instant.now());
        } catch (RuntimeException exception) {
            var attempt = delivery.get().attempts();
            var delay = retryBaseSeconds * (1L << Math.min(Math.max(attempt - 1, 0), 10));
            var nextAttemptAt = Instant.now().plusSeconds(Math.min(delay, 86_400));
            outboxStore.markFailed(outboxId, nextAttemptAt, sanitizedError(exception));
        }
    }

    private static String sanitizedError(RuntimeException exception) {
        var message = exception.getClass().getSimpleName();
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            message += ": " + URL.matcher(exception.getMessage()).replaceAll("[url]")
                .replaceAll("[\\r\\n]+", " ");
        }
        return message.length() <= MAX_ERROR_LENGTH
            ? message
            : message.substring(0, MAX_ERROR_LENGTH);
    }
}
