package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ProductNotFoundException;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class PublicProductFinder {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PublicProductFinder(
        ProductRepository productRepository,
        CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public VisibleProduct byId(UUID productId) {
        return visible(() -> productRepository.findByIdAndActiveTrue(productId).orElseThrow(
            ProductNotFoundException::new
        ));
    }

    public VisibleProduct bySlug(String slug) {
        return visible(() -> productRepository.findBySlugAndActiveTrue(slug).orElseThrow(
            ProductNotFoundException::new
        ));
    }

    private VisibleProduct visible(Supplier<Product> productSupplier) {
        var product = productSupplier.get();
        var category = categoryRepository.findByIdAndActiveTrue(product.getCategoryId())
            .orElseThrow(ProductNotFoundException::new);
        return new VisibleProduct(product, category);
    }

    public record VisibleProduct(Product product, Category category) {
    }
}
