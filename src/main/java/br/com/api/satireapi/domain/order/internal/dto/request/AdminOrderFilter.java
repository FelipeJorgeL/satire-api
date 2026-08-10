package br.com.api.satireapi.domain.order.internal.dto.request;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminOrderFilter(
    OrderStatus status,
    UUID customerId,
    OffsetDateTime from,
    OffsetDateTime to
) {
}
