package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ProductNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicProductFinderTest {

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private PublicProductFinder finder;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        finder = new PublicProductFinder(productRepository, categoryRepository);
    }

    @Test
    void returnsProductOnlyWhenItsCategoryIsActive() {
        var productId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var product = mock(Product.class);
        var category = mock(Category.class);
        when(product.getCategoryId()).thenReturn(categoryId);
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findByIdAndActiveTrue(categoryId)).thenReturn(Optional.of(category));

        var result = finder.byId(productId);

        assertSame(product, result.product());
        assertSame(category, result.category());
    }

    @Test
    void hidesProductWhenItsCategoryIsInactive() {
        var productId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var product = mock(Product.class);
        when(product.getCategoryId()).thenReturn(categoryId);
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.of(product));
        when(categoryRepository.findByIdAndActiveTrue(categoryId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> finder.byId(productId));
    }

    @Test
    void hidesInactiveOrMissingProduct() {
        var productId = UUID.randomUUID();
        when(productRepository.findByIdAndActiveTrue(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> finder.byId(productId));
    }
}
