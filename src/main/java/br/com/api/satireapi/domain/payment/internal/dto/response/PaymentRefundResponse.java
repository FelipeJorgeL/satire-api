package br.com.api.satireapi.domain.payment.internal.dto.response;

import br.com.api.satireapi.domain.payment.internal.model.RefundStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentRefundResponse(
    UUID id,
    UUID paymentId,
    RefundStatus status,
    BigDecimal amount,
    String reason,
    OffsetDateTime createdAt
) {
}
