package br.com.api.satireapi.domain.payment.internal.usecase.webhook;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.model.Payment;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import br.com.api.satireapi.domain.payment.internal.model.PaymentWebhookEvent;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentWebhookEventRepository;
import br.com.api.satireapi.domain.payment.internal.security.InvalidPaymentWebhookSignatureException;
import br.com.api.satireapi.domain.payment.internal.security.PaymentWebhookVerifier;
import br.com.api.satireapi.domain.payment.internal.usecase.InvalidPaymentWebhookPayloadException;
import br.com.api.satireapi.domain.payment.internal.usecase.WebhookReplayConflictException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ProcessPaymentWebhookUseCaseTest {

    private PaymentWebhookVerifier verifier;
    private PaymentRepository paymentRepository;
    private PaymentWebhookEventRepository eventRepository;
    private PaymentOrderGateway orderGateway;
    private ProcessPaymentWebhookUseCase useCase;

    @BeforeEach
    void setUp() {
        verifier = mock(PaymentWebhookVerifier.class);
        paymentRepository = mock(PaymentRepository.class);
        eventRepository = mock(PaymentWebhookEventRepository.class);
        orderGateway = mock(PaymentOrderGateway.class);
        useCase = new ProcessPaymentWebhookUseCase(
            verifier, new ObjectMapper(), paymentRepository, eventRepository, orderGateway
        );
    }

    @Test
    void appliesApprovedWebhookAndMarksOrderPaid() {
        var paymentId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var payment = mock(Payment.class);
        var payload = approvedPayload("txn-approved");
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(payment.getOrderId()).thenReturn(orderId);
        when(eventRepository.insertIfAbsent(
            org.mockito.ArgumentMatchers.eq("evt-12345678"),
            org.mockito.ArgumentMatchers.eq(paymentId),
            org.mockito.ArgumentMatchers.any()
        )).thenReturn(1);

        useCase.execute(paymentId, "evt-12345678", "timestamp", "signature", payload);

        verify(verifier).verify("evt-12345678", "timestamp", "signature", payload);
        verify(payment).applyGatewayUpdate(PaymentStatus.APROVADO, "txn-approved");
        verify(orderGateway).markPaid(orderId);
    }

    @Test
    void acceptsExactEventReplayWithoutApplyingAgain() {
        var paymentId = UUID.randomUUID();
        var payment = mock(Payment.class);
        var event = mock(PaymentWebhookEvent.class);
        var payload = approvedPayload("txn-approved");
        var hash = sha256(payload);
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(eventRepository.insertIfAbsent("evt-12345678", paymentId, hash)).thenReturn(0);
        when(eventRepository.findById("evt-12345678")).thenReturn(Optional.of(event));
        when(event.getPaymentId()).thenReturn(paymentId);
        when(event.getPayloadHash()).thenReturn(hash);

        useCase.execute(paymentId, "evt-12345678", "timestamp", "signature", payload);

        verify(payment, never()).applyGatewayUpdate(
            org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsEventIdReusedForDifferentPayment() {
        var paymentId = UUID.randomUUID();
        var payment = mock(Payment.class);
        var event = mock(PaymentWebhookEvent.class);
        var payload = approvedPayload("txn-approved");
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(eventRepository.insertIfAbsent(
            org.mockito.ArgumentMatchers.eq("evt-12345678"),
            org.mockito.ArgumentMatchers.eq(paymentId),
            org.mockito.ArgumentMatchers.any()
        )).thenReturn(0);
        when(eventRepository.findById("evt-12345678")).thenReturn(Optional.of(event));
        when(event.getPaymentId()).thenReturn(UUID.randomUUID());

        assertThrows(
            WebhookReplayConflictException.class,
            () -> useCase.execute(
                paymentId, "evt-12345678", "timestamp", "signature", payload
            )
        );
    }

    @Test
    void rejectsInvalidPayloadAfterSignatureValidation() {
        assertThrows(
            InvalidPaymentWebhookPayloadException.class,
            () -> useCase.execute(
                UUID.randomUUID(), "evt-12345678", "timestamp", "signature", "{}"
            )
        );
        verify(verifier).verify("evt-12345678", "timestamp", "signature", "{}");
    }

    @Test
    void invalidSignatureStopsBeforeDatabaseAccess() {
        var paymentId = UUID.randomUUID();
        var payload = approvedPayload("txn-approved");
        org.mockito.Mockito.doThrow(new InvalidPaymentWebhookSignatureException())
            .when(verifier).verify("evt-12345678", "timestamp", "invalid", payload);

        assertThrows(
            InvalidPaymentWebhookSignatureException.class,
            () -> useCase.execute(
                paymentId, "evt-12345678", "timestamp", "invalid", payload
            )
        );
        verify(paymentRepository, never()).findByIdForUpdate(paymentId);
    }

    private String approvedPayload(String transactionId) {
        return "{\"status\":\"APROVADO\",\"gatewayTransactionId\":\""
            + transactionId + "\"}";
    }

    private String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8))
            );
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new AssertionError(exception);
        }
    }
}
