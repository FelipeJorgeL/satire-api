package br.com.api.satireapi.domain.catalog.internal.dto.response;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import java.math.BigDecimal;
import java.util.UUID;

public record PublicProductListItemResponse(
    UUID id,
    String name,
    String slug,
    String description,
    CategorySummary category,
    BigDecimal minimumPrice,
    ProductImageSummary primaryImage
) {
}
