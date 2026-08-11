package br.com.api.satireapi.domain.catalog.internal.usecase;

import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.ProductMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.JpaVariationFinder;

import java.util.List;
import java.util.UUID;

public class FindVariationUsecase {
    private final JpaVariationFinder finder;
    private final ProductMapper mapper;


    public FindVariationUsecase(JpaVariationFinder finder, ProductMapper mapper) {
        this.finder = finder;
        this.mapper = mapper;
    }

    public List<ProductVariationSummary> findVariationByProductId(UUID id) {
        if (id == null) {
            throw new InvalidIdException();
        }
        return finder.findActiveByProductId(id).stream().map(mapper::productVariationToSummary).toList();
    }
}
