package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.time.OffsetDateTime;

public record FavoriteResponse(
    OffsetDateTime favoritedAt,
    PublicProductListItemResponse product
) {
}
