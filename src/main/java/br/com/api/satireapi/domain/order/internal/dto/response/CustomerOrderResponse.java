package br.com.api.satireapi.domain.order.internal.dto.response;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerOrderResponse(
    UUID id,
    String number,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal shippingFee,
    BigDecimal total,
    OffsetDateTime createdAt
) {
}
