package br.com.api.satireapi.domain.cart;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemSnapshot(
    UUID itemId,
    UUID variationId,
    UUID productId,
    String productName,
    String sku,
    String variationName,
    BigDecimal unitPrice,
    int stock,
    boolean active,
    int quantity
) {
}
