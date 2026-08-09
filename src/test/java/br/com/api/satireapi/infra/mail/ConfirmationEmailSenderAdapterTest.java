package br.com.api.satireapi.infra.mail;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailDelivery;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailOutboxStore;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationEmailSender;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationLinkProtector;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class ConfirmationEmailSenderAdapterTest {

    private final ConfirmationEmailOutboxStore outboxStore = mock(ConfirmationEmailOutboxStore.class);
    private final ConfirmationEmailSender emailSender = mock(ConfirmationEmailSender.class);
    private final ConfirmationLinkProtector linkProtector = mock(ConfirmationLinkProtector.class);
    private final EmailDeliveryRateLimiter rateLimiter = mock(EmailDeliveryRateLimiter.class);
    private final ConfirmationEmailSenderAdapter adapter = new ConfirmationEmailSenderAdapter(
        outboxStore,
        emailSender,
        linkProtector,
        rateLimiter,
        Duration.ofSeconds(1)
    );

    @Test
    void marksDeliveryAsSentAfterProviderAcceptsIt() {
        var id = UUID.randomUUID();
        when(rateLimiter.tryAcquire(any(Instant.class))).thenReturn(true);
        when(outboxStore.claim(any(), any())).thenReturn(Optional.of(
            new ConfirmationEmailDelivery(id, "felipe@example.com", "encrypted", 1)));
        when(linkProtector.unprotect("encrypted")).thenReturn("http://localhost/confirm?token=opaque");

        adapter.dispatch(id);

        verify(emailSender).sendEmailConfirmation("felipe@example.com", "http://localhost/confirm?token=opaque");
        verify(outboxStore).markSent(any(), any());
    }

    @Test
    void recordsSanitizedFailureForRetry() {
        var id = UUID.randomUUID();
        when(rateLimiter.tryAcquire(any(Instant.class))).thenReturn(true);
        when(outboxStore.claim(any(), any())).thenReturn(Optional.of(
            new ConfirmationEmailDelivery(id, "felipe@example.com", "encrypted", 1)));
        when(linkProtector.unprotect("encrypted")).thenReturn("http://localhost/confirm?token=opaque");
        doThrow(new RestClientException("request failed: https://provider.invalid/opaque-token"))
            .when(emailSender).sendEmailConfirmation(any(), any());

        adapter.dispatch(id);

        verify(outboxStore).markFailed(any(), any(), org.mockito.ArgumentMatchers.argThat(
            failure -> failure.contains("RestClientException") && !failure.contains("opaque-token")
        ));
    }
}
