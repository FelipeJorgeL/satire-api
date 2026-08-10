package br.com.api.satireapi.domain.order.internal.dto.response;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminOrderListItemResponse(
    UUID id,
    UUID customerId,
    String number,
    OrderStatus status,
    BigDecimal total,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
