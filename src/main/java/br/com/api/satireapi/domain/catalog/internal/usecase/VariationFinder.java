package br.com.api.satireapi.domain.catalog.internal.usecase;

import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;

import java.util.List;
import java.util.UUID;

public interface VariationFinder {
    List<ProductVariation> findActiveByProductId(UUID id);
}
