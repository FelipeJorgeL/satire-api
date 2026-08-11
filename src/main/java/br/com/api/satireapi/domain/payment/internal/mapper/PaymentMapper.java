package br.com.api.satireapi.domain.payment.internal.mapper;

import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentRefundResponse;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.model.Payment;
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

    public static PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(), payment.getOrderId(), payment.getMethod(), payment.getStatus(),
            payment.getAmount(), payment.getGatewayTransactionId(), payment.getPaidAt(),
            payment.getCreatedAt(), payment.getUpdatedAt()
        );
    }
}
