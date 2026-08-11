package br.com.api.satireapi.domain.inventory.internal.dto.response;

import java.util.UUID;

public record ProductVariationAvailabilityResponse(
    UUID productId,
    UUID variationId,
    boolean available,
    int availableQuantity
) {
}
