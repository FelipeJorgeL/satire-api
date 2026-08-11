package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPublicProductUseCase {

    private final PublicProductFinder productFinder;
    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;

    public GetPublicProductUseCase(
        PublicProductFinder productFinder,
        ProductVariationRepository variationRepository,
        ProductImageRepository imageRepository
    ) {
        this.productFinder = productFinder;
        this.variationRepository = variationRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true)
    public PublicProductDetailsResponse byId(UUID productId) {
        return details(productFinder.byId(productId));
    }

    @Transactional(readOnly = true)
    public PublicProductDetailsResponse bySlug(String slug) {
        return details(productFinder.bySlug(slug));
    }

    private PublicProductDetailsResponse details(PublicProductFinder.VisibleProduct visible) {
        var productId = visible.product().getId();
        return PublicCatalogMapper.toDetails(
            visible.product(), visible.category(),
            variationRepository.findAllByProductIdAndActiveTrueOrderByCreatedAtAsc(productId),
            imageRepository.findAllByProductIdOrderByDisplayOrderAsc(productId)
        );
    }
}
