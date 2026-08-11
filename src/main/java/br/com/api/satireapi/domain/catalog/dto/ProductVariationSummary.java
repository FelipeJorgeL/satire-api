package br.com.api.satireapi.domain.catalog.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductVariationSummary(
        UUID id,
        String name,
        String sku,
        BigDecimal price,
        int inventory,
        boolean active
) {}