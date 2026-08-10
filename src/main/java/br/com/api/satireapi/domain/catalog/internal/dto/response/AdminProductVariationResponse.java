package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AdminProductVariationResponse(
    UUID id,
    String sku,
    String name,
    BigDecimal price,
    int stock,
    boolean active
) {
}
