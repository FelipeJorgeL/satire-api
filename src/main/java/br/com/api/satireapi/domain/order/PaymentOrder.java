package br.com.api.satireapi.domain.order;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentOrder(
    UUID id,
    UUID customerId,
    OrderStatus status,
    BigDecimal total
) {
}
