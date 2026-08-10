package br.com.api.satireapi.domain.payment.internal.mapper;

import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentRefundResponse;
import br.com.api.satireapi.domain.payment.internal.model.RefundRequest;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentRefundResponse toResponse(RefundRequest refund) {
        return new PaymentRefundResponse(
            refund.getId(), refund.getPaymentId(), refund.getStatus(), refund.getAmount(),
            refund.getReason(), refund.getCreatedAt()
        );
    }
}
