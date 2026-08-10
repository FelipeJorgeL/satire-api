package br.com.api.satireapi.domain.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariationPurchase(
    UUID variationId,
    UUID productId,
    String productName,
    String sku,
    String variationName,
    BigDecimal unitPrice,
    int stock,
    boolean active
) {
}
