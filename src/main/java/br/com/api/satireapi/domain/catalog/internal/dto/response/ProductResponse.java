package br.com.api.satireapi.domain.catalog.internal.dto.response;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;

import java.util.List;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        boolean active,
        CategorySummary category,
        List<ProductImageSummary> images,
        List<ProductVariationSummary> variations
) {}