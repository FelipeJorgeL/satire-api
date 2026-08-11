package br.com.api.satireapi.domain.catalog.internal.usecase;


import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.ProductMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.JpaImageFinder;

import java.util.List;
import java.util.UUID;

public class FindImageUseCase {
    private final JpaImageFinder finder;
    private final ProductMapper mapper;

    public FindImageUseCase(JpaImageFinder finder, ProductMapper mapper) {
        this.finder = finder;
        this.mapper = mapper;
    }

    public List<ProductImageSummary> findActiveByProductId(UUID id) {
        if (id == null) {
            throw new InvalidIdException();
        }
        return finder.findActiveByProductId(id).stream().map(mapper::productImageToSummary).toList();
    }
}
