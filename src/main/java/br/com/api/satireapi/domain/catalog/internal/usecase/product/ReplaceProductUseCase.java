package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.dto.request.ProductRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.CatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.CategoryNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReplaceProductUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAggregateWriter aggregateWriter;

    public ReplaceProductUseCase(
        CategoryRepository categoryRepository,
        ProductRepository productRepository,
        ProductVariationRepository variationRepository,
        ProductImageRepository imageRepository,
        ProductAggregateWriter aggregateWriter
    ) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.variationRepository = variationRepository;
        this.imageRepository = imageRepository;
        this.aggregateWriter = aggregateWriter;
    }

    @Transactional
    public AdminProductDetailsResponse execute(UUID productId, ProductRequest request) {
        var product = productRepository.findByIdForUpdate(productId)
            .orElseThrow(ProductNotFoundException::new);
        categoryRepository.findByIdAndActiveTrue(request.categoryId())
            .orElseThrow(CategoryNotFoundException::new);
        var slug = request.slug().trim();
        if (productRepository.existsBySlugAndIdNot(slug, productId)) {
            throw new ProductSlugAlreadyExistsException();
        }
        product.replace(request.categoryId(), request.name(), slug, request.description());
        aggregateWriter.replaceChildren(product, request);
        return CatalogMapper.toDetails(
            product,
            variationRepository.findAllByProductIdOrderByCreatedAtAsc(productId),
            imageRepository.findAllByProductIdOrderByDisplayOrderAsc(productId)
        );
    }
}
