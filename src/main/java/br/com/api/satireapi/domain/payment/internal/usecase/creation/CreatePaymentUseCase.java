package br.com.api.satireapi.domain.payment.internal.usecase.creation;

import br.com.api.satireapi.domain.order.OrderPaymentNotAllowedException;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.dto.request.CreatePaymentRequest;
import br.com.api.satireapi.domain.payment.internal.mapper.PaymentMapper;
import br.com.api.satireapi.domain.payment.internal.model.Payment;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.IdempotencyKeyConflictException;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentOrderNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderGateway orderGateway;

    public CreatePaymentUseCase(
        PaymentRepository paymentRepository,
        PaymentOrderGateway orderGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.orderGateway = orderGateway;
    }

    @Transactional
    public PaymentCreationResult execute(
        UUID customerId,
        UUID orderId,
        String idempotencyKey,
        CreatePaymentRequest request
    ) {
        var normalizedKey = idempotencyKey.trim();
        var existing = paymentRepository.findByIdempotencyKey(normalizedKey);
        if (existing.isPresent()) {
            return replay(customerId, orderId, request, existing.get());
        }

        var order = orderGateway.findOwnedForUpdate(customerId, orderId)
            .orElseThrow(PaymentOrderNotFoundException::new);
        if (order.status() != OrderStatus.AGUARDANDO_PAGAMENTO) {
            throw new OrderPaymentNotAllowedException();
        }

        var paymentId = UUID.randomUUID();
        var inserted = paymentRepository.insertPendingIfAbsent(
            paymentId, orderId, request.method().name(), order.total(), normalizedKey
        );
        var payment = paymentRepository.findByIdempotencyKey(normalizedKey)
            .orElseThrow(() -> new IllegalStateException("Pagamento idempotente não foi persistido"));
        if (!payment.getOrderId().equals(orderId) || payment.getMethod() != request.method()) {
            throw new IdempotencyKeyConflictException();
        }
        return new PaymentCreationResult(PaymentMapper.toResponse(payment), inserted == 1);
    }

    private PaymentCreationResult replay(
        UUID customerId,
        UUID orderId,
        CreatePaymentRequest request,
        Payment payment
    ) {
        if (!orderGateway.isOwnedBy(customerId, payment.getOrderId())) {
            throw new PaymentOrderNotFoundException();
        }
        if (!payment.getOrderId().equals(orderId) || payment.getMethod() != request.method()) {
            throw new IdempotencyKeyConflictException();
        }
        return new PaymentCreationResult(PaymentMapper.toResponse(payment), false);
    }
}
