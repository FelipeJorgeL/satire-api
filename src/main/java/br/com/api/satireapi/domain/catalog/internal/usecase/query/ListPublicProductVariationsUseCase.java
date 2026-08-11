package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublicProductVariationsUseCase {

    private final PublicProductFinder productFinder;
    private final ProductVariationRepository variationRepository;

    public ListPublicProductVariationsUseCase(
        PublicProductFinder productFinder,
        ProductVariationRepository variationRepository
    ) {
        this.productFinder = productFinder;
        this.variationRepository = variationRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductVariationSummary> execute(UUID productId) {
        productFinder.byId(productId);
        return variationRepository.findAllByProductIdAndActiveTrueOrderByCreatedAtAsc(productId)
            .stream()
            .map(PublicCatalogMapper::toVariation)
            .toList();
    }
}
