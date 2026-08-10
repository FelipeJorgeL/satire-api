package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminProductDetailsResponse(
    UUID id,
    UUID categoryId,
    String name,
    String slug,
    String description,
    boolean active,
    OffsetDateTime createdAt,
    List<AdminProductVariationResponse> variations,
    List<AdminProductImageResponse> images
) {
}
