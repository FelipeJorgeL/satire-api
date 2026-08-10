package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.usecase.ImageFinder;

import java.util.List;
import java.util.UUID;

public class JpaImageFinder implements ImageFinder {
    private final JpaImageRepository repository;

    public JpaImageFinder(JpaImageRepository repository) {
        this.repository = repository;
    }

    public List<ProductImage> findActiveByProductId(UUID id) {
        return repository.findByProductIdAndActiveTrueOrderByDisplayOrder(id);
    }
}
