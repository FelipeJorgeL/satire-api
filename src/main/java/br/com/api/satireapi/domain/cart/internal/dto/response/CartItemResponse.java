package br.com.api.satireapi.domain.cart.internal.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
    UUID id,
    UUID variationId,
    UUID productId,
    String productName,
    String sku,
    String variationName,
    BigDecimal unitPrice,
    int quantity,
    BigDecimal subtotal,
    int currentStock,
    boolean available
) {
}
