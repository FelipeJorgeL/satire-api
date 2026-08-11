package br.com.api.satireapi.domain.catalog.internal.persistence;


import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import br.com.api.satireapi.domain.catalog.internal.usecase.VariationFinder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaVariationFinder implements VariationFinder {
    private final JpaVariationRepository repository;

    public JpaVariationFinder(JpaVariationRepository repository) {
        this.repository = repository;
    }

    public List<ProductVariation> findActiveByProductId(UUID id) {
        return repository.findByProductIdAndActiveTrue(id);
    }
}
