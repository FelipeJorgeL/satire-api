package br.com.api.satireapi.domain.catalog.dto;

import java.util.UUID;

public record ProductVariationSummary(
        UUID id,
        String name,
        String sku,
        double price,
        int inventory,
        boolean active
) {}