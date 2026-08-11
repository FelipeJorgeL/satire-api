package br.com.api.satireapi.domain.order.internal.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CustomerOrderItemResponse(
    UUID id,
    UUID variationId,
    String sku,
    String productName,
    String variationName,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal subtotal
) {
}
