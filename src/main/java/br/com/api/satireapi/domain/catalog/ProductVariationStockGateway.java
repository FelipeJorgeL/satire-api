package br.com.api.satireapi.domain.catalog;

import java.util.Optional;
import java.util.UUID;

public interface ProductVariationStockGateway {

    Optional<ProductVariationStock> findById(UUID variationId);

    Optional<ProductVariationStock> findPublicAvailability(UUID productId, UUID variationId);

    Optional<ProductVariationStock> findByIdForUpdate(UUID variationId);

    ProductVariationStockAdjustment adjustStock(UUID variationId, int delta);
}
