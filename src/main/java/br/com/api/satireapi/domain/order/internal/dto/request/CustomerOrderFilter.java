package br.com.api.satireapi.domain.order.internal.dto.request;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.time.OffsetDateTime;

public record CustomerOrderFilter(
    OrderStatus status,
    OffsetDateTime from,
    OffsetDateTime to
) {
}
