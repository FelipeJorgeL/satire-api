package br.com.api.satireapi.domain.catalog;

import java.util.UUID;

public record ProductVariationStock(
    UUID variationId,
    UUID productId,
    String sku,
    String name,
    int stock,
    boolean active
) {
}
