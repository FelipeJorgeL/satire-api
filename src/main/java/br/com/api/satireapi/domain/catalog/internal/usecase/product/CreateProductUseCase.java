package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.dto.request.ProductRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.CatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.CategoryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProductUseCase {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;
    private final ProductAggregateWriter aggregateWriter;

    public CreateProductUseCase(
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
    public AdminProductDetailsResponse execute(ProductRequest request) {
        requireActiveCategory(request.categoryId());
        var slug = request.slug().trim();
        if (productRepository.existsBySlug(slug)) {
            throw new ProductSlugAlreadyExistsException();
        }
        var product = productRepository.save(Product.create(
            request.categoryId(), request.name(), slug, request.description()
        ));
        aggregateWriter.replaceChildren(product, request);
        return CatalogMapper.toDetails(
            product,
            variationRepository.findAllByProductIdOrderByCreatedAtAsc(product.getId()),
            imageRepository.findAllByProductIdOrderByDisplayOrderAsc(product.getId())
        );
    }

    private void requireActiveCategory(java.util.UUID categoryId) {
        categoryRepository.findByIdAndActiveTrue(categoryId)
            .orElseThrow(CategoryNotFoundException::new);
    }
}
