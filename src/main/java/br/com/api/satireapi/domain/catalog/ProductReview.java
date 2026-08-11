package br.com.api.satireapi.domain.catalog;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductReview(
    UUID id,
    UUID productId,
    short rating,
    String comment,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
