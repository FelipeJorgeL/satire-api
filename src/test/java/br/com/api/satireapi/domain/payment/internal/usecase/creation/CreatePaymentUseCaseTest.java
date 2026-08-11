package br.com.api.satireapi.domain.payment.internal.usecase.creation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.order.OrderPaymentNotAllowedException;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.PaymentOrder;
import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.dto.request.CreatePaymentRequest;
import br.com.api.satireapi.domain.payment.internal.model.Payment;
import br.com.api.satireapi.domain.payment.internal.model.PaymentMethod;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.IdempotencyKeyConflictException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatePaymentUseCaseTest {

    private PaymentRepository paymentRepository;
    private PaymentOrderGateway orderGateway;
    private CreatePaymentUseCase useCase;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderGateway = mock(PaymentOrderGateway.class);
        useCase = new CreatePaymentUseCase(paymentRepository, orderGateway);
    }

    @Test
    void createsPendingPaymentWithServerSideOrderAmount() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var key = "payment-key-123456";
        var payment = payment(orderId, key, PaymentMethod.PIX);
        when(paymentRepository.findByIdempotencyKey(key))
            .thenReturn(Optional.empty(), Optional.of(payment));
        when(orderGateway.findOwnedForUpdate(customerId, orderId)).thenReturn(Optional.of(
            new PaymentOrder(
                orderId, customerId, OrderStatus.AGUARDANDO_PAGAMENTO, new BigDecimal("149.90")
            )
        ));
        when(paymentRepository.insertPendingIfAbsent(
            org.mockito.ArgumentMatchers.any(), eq(orderId), eq("PIX"),
            eq(new BigDecimal("149.90")), eq(key)
        )).thenReturn(1);

        var result = useCase.execute(
            customerId, orderId, key, new CreatePaymentRequest(PaymentMethod.PIX)
        );

        assertTrue(result.created());
        verify(paymentRepository).insertPendingIfAbsent(
            org.mockito.ArgumentMatchers.any(), eq(orderId), eq("PIX"),
            eq(new BigDecimal("149.90")), eq(key)
        );
    }

    @Test
    void replaysSameRequestWithoutCreatingAnotherPayment() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var key = "payment-key-123456";
        var payment = payment(orderId, key, PaymentMethod.CARTAO);
        when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(payment));
        when(orderGateway.isOwnedBy(customerId, orderId)).thenReturn(true);

        var result = useCase.execute(
            customerId, orderId, key, new CreatePaymentRequest(PaymentMethod.CARTAO)
        );

        assertFalse(result.created());
        verify(paymentRepository, never()).insertPendingIfAbsent(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()
        );
    }

    @Test
    void rejectsReusedKeyWithDifferentMethod() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var key = "payment-key-123456";
        var payment = payment(orderId, key, PaymentMethod.PIX);
        when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(payment));
        when(orderGateway.isOwnedBy(customerId, orderId)).thenReturn(true);

        assertThrows(
            IdempotencyKeyConflictException.class,
            () -> useCase.execute(
                customerId, orderId, key, new CreatePaymentRequest(PaymentMethod.BOLETO)
            )
        );
    }

    @Test
    void rejectsPaymentWhenOrderIsNoLongerAwaitingPayment() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var key = "payment-key-123456";
        when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(orderGateway.findOwnedForUpdate(customerId, orderId)).thenReturn(Optional.of(
            new PaymentOrder(orderId, customerId, OrderStatus.PAGO, new BigDecimal("149.90"))
        ));

        assertThrows(
            OrderPaymentNotAllowedException.class,
            () -> useCase.execute(
                customerId, orderId, key, new CreatePaymentRequest(PaymentMethod.PIX)
            )
        );
    }

    private Payment payment(UUID orderId, String key, PaymentMethod method) {
        var payment = mock(Payment.class);
        when(payment.getId()).thenReturn(UUID.randomUUID());
        when(payment.getOrderId()).thenReturn(orderId);
        when(payment.getMethod()).thenReturn(method);
        when(payment.getStatus()).thenReturn(PaymentStatus.PENDENTE);
        when(payment.getAmount()).thenReturn(new BigDecimal("149.90"));
        when(payment.getIdempotencyKey()).thenReturn(key);
        when(payment.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(payment.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        return payment;
    }
}
