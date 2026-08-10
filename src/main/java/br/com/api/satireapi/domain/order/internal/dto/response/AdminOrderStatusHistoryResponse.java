package br.com.api.satireapi.domain.order.internal.dto.response;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminOrderStatusHistoryResponse(
    UUID id,
    UUID userId,
    OrderStatus previousStatus,
    OrderStatus newStatus,
    String reason,
    OffsetDateTime createdAt
) {
}
