package br.com.api.satireapi.domain.catalog;

import java.util.UUID;

public record ProductVariationStockAdjustment(
    UUID variationId,
    UUID productId,
    String sku,
    String name,
    int previousStock,
    int newStock,
    boolean active
) {
}
