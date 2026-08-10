package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminProductListItemResponse(
    UUID id,
    UUID categoryId,
    String name,
    String slug,
    boolean active,
    OffsetDateTime createdAt
) {
}
