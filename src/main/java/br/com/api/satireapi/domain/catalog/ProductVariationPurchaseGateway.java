package br.com.api.satireapi.domain.catalog;

import java.util.Optional;
import java.util.UUID;

public interface ProductVariationPurchaseGateway {

    Optional<ProductVariationPurchase> findById(UUID variationId);
}
