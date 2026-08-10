package br.com.api.satireapi.domain.payment.internal.usecase.refund;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.payment.internal.dto.request.RequestPaymentRefund;
import br.com.api.satireapi.domain.payment.internal.model.Payment;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import br.com.api.satireapi.domain.payment.internal.model.RefundRequest;
import br.com.api.satireapi.domain.payment.internal.model.RefundStatus;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.persistence.RefundRequestRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.IdempotencyKeyConflictException;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentNotRefundableException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RequestPaymentRefundUseCaseTest {

    private final PaymentRepository paymentRepository = mock(PaymentRepository.class);
    private final RefundRequestRepository refundRequestRepository = mock(RefundRequestRepository.class);
    private final RequestPaymentRefundUseCase useCase = new RequestPaymentRefundUseCase(
        paymentRepository, refundRequestRepository
    );

    @Test
    void createsSolicitedRefundForApprovedPayment() {
        var paymentId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var payment = mock(Payment.class);
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(payment.isRefundable()).thenReturn(true);
        when(payment.getAmount()).thenReturn(new BigDecimal("149.90"));
        when(refundRequestRepository.findByIdempotencyKey("refund-key-123456"))
            .thenReturn(Optional.empty());
        when(refundRequestRepository.existsByPaymentId(paymentId)).thenReturn(false);
        when(refundRequestRepository.save(any(RefundRequest.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(
            adminId,
            paymentId,
            "refund-key-123456",
            new RequestPaymentRefund("Cliente solicitou estorno")
        );

        assertEquals(paymentId, response.paymentId());
        assertEquals(RefundStatus.SOLICITADO, response.status());
        assertEquals(new BigDecimal("149.90"), response.amount());
        verify(paymentRepository).findByIdForUpdate(paymentId);
    }

    @Test
    void replaysTheSameRequestForTheSameIdempotencyKey() {
        var paymentId = UUID.randomUUID();
        var refund = mock(RefundRequest.class);
        when(refund.getPaymentId()).thenReturn(paymentId);
        when(refund.getStatus()).thenReturn(RefundStatus.SOLICITADO);
        when(refund.getAmount()).thenReturn(BigDecimal.TEN);
        when(refund.getReason()).thenReturn("Solicitação duplicada");
        when(refund.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(refundRequestRepository.findByIdempotencyKey("refund-key-123456"))
            .thenReturn(Optional.of(refund));

        var response = useCase.execute(
            UUID.randomUUID(), paymentId, "refund-key-123456",
            new RequestPaymentRefund("Qualquer motivo")
        );

        assertEquals(RefundStatus.SOLICITADO, response.status());
        verify(paymentRepository, never()).findByIdForUpdate(any());
        verify(refundRequestRepository, never()).save(any());
    }

    @Test
    void rejectsReuseOfKeyForAnotherPayment() {
        var firstPaymentId = UUID.randomUUID();
        var secondPaymentId = UUID.randomUUID();
        var refund = mock(RefundRequest.class);
        when(refund.getPaymentId()).thenReturn(firstPaymentId);
        when(refundRequestRepository.findByIdempotencyKey("refund-key-123456"))
            .thenReturn(Optional.of(refund));

        assertThrows(
            IdempotencyKeyConflictException.class,
            () -> useCase.execute(
                UUID.randomUUID(), secondPaymentId, "refund-key-123456",
                new RequestPaymentRefund("Motivo")
            )
        );
    }

    @Test
    void rejectsPaymentThatIsNotApproved() {
        var paymentId = UUID.randomUUID();
        var payment = mock(Payment.class);
        when(paymentRepository.findByIdForUpdate(paymentId)).thenReturn(Optional.of(payment));
        when(payment.isRefundable()).thenReturn(false);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDENTE);
        when(refundRequestRepository.findByIdempotencyKey("refund-key-123456"))
            .thenReturn(Optional.empty());

        assertThrows(
            PaymentNotRefundableException.class,
            () -> useCase.execute(
                UUID.randomUUID(), paymentId, "refund-key-123456",
                new RequestPaymentRefund("Motivo")
            )
        );
    }
}
