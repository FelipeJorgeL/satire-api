package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublicProductImagesUseCase {

    private final PublicProductFinder productFinder;
    private final ProductImageRepository imageRepository;

    public ListPublicProductImagesUseCase(
        PublicProductFinder productFinder,
        ProductImageRepository imageRepository
    ) {
        this.productFinder = productFinder;
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductImageSummary> execute(UUID productId) {
        productFinder.byId(productId);
        return imageRepository.findAllByProductIdOrderByDisplayOrderAsc(productId)
            .stream()
            .map(PublicCatalogMapper::toImage)
            .toList();
    }
}
