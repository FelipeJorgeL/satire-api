package br.com.api.satireapi.domain.payment.internal.usecase.refund;

import br.com.api.satireapi.domain.payment.internal.dto.request.RequestPaymentRefund;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentRefundResponse;
import br.com.api.satireapi.domain.payment.internal.mapper.PaymentMapper;
import br.com.api.satireapi.domain.payment.internal.model.RefundRequest;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.persistence.RefundRequestRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.IdempotencyKeyConflictException;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentNotFoundException;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentNotRefundableException;
import br.com.api.satireapi.domain.payment.internal.usecase.RefundAlreadyRequestedException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestPaymentRefundUseCase {

    private final PaymentRepository paymentRepository;
    private final RefundRequestRepository refundRequestRepository;

    public RequestPaymentRefundUseCase(
        PaymentRepository paymentRepository,
        RefundRequestRepository refundRequestRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.refundRequestRepository = refundRequestRepository;
    }

    @Transactional
    public PaymentRefundResponse execute(
        UUID adminId,
        UUID paymentId,
        String idempotencyKey,
        RequestPaymentRefund request
    ) {
        var normalizedKey = idempotencyKey.trim();
        var existing = refundRequestRepository.findByIdempotencyKey(normalizedKey);
        if (existing.isPresent()) {
            if (!existing.get().getPaymentId().equals(paymentId)) {
                throw new IdempotencyKeyConflictException();
            }
            return PaymentMapper.toResponse(existing.get());
        }

        var payment = paymentRepository.findByIdForUpdate(paymentId)
            .orElseThrow(PaymentNotFoundException::new);
        if (!payment.isRefundable()) {
            throw new PaymentNotRefundableException(payment.getStatus());
        }
        if (refundRequestRepository.existsByPaymentId(paymentId)) {
            throw new RefundAlreadyRequestedException();
        }

        var refund = RefundRequest.request(
            paymentId, adminId, normalizedKey, payment.getAmount(), request.reason()
        );
        return PaymentMapper.toResponse(refundRequestRepository.save(refund));
    }
}
