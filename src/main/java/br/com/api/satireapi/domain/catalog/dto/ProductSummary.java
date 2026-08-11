package br.com.api.satireapi.domain.catalog.dto;

import java.util.List;
import java.util.UUID;

public record ProductSummary (
        UUID id,
        String name,
        String slug,
        String description,
        boolean active,
        CategorySummary category,
        List<ProductImageSummary> images,
        List<ProductVariationSummary> variations
) {}