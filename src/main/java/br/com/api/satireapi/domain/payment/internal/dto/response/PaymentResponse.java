package br.com.api.satireapi.domain.payment.internal.dto.response;

import br.com.api.satireapi.domain.payment.internal.model.PaymentMethod;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
    UUID id,
    UUID orderId,
    PaymentMethod method,
    PaymentStatus status,
    BigDecimal amount,
    String gatewayTransactionId,
    OffsetDateTime paidAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
