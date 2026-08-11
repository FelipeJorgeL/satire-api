package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.internal.dto.request.PublicProductFilter;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublicProductsUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;

    public ListPublicProductsUseCase(
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        ProductVariationRepository variationRepository,
        ProductImageRepository imageRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variationRepository = variationRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true)
    public Page<PublicProductListItemResponse> execute(
        PublicProductFilter filter,
        Pageable pageable
    ) {
        var products = productRepository.findAll(specification(filter), pageable);
        if (products.isEmpty()) {
            return Page.empty(pageable);
        }

        var productIds = products.stream().map(Product::getId).toList();
        var categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();
        var categories = categoryRepository.findAllById(categoryIds).stream()
            .filter(Category::isActive)
            .collect(Collectors.toMap(Category::getId, Function.identity()));
        var variations = variationRepository
            .findAllByProductIdInAndActiveTrueOrderByCreatedAtAsc(productIds).stream()
            .collect(Collectors.groupingBy(ProductVariation::getProductId));
        var images = imageRepository
            .findAllByProductIdInOrderByProductIdAscDisplayOrderAsc(productIds).stream()
            .collect(Collectors.groupingBy(ProductImage::getProductId));

        return products.map(product -> PublicCatalogMapper.toListItem(
            product,
            requiredCategory(categories, product.getCategoryId()),
            variations.getOrDefault(product.getId(), List.of()),
            images.getOrDefault(product.getId(), List.of())
        ));
    }

    private Specification<Product> specification(PublicProductFilter filter) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(builder.isTrue(root.get("active")));

            var activeCategories = query.subquery(UUID.class);
            var category = activeCategories.from(Category.class);
            activeCategories.select(category.get("id"))
                .where(builder.isTrue(category.get("active")));
            predicates.add(root.get("categoryId").in(activeCategories));

            if (filter.search() != null && !filter.search().isBlank()) {
                var search = "%" + escapeLike(filter.search().trim().toLowerCase(Locale.ROOT)) + "%";
                predicates.add(builder.or(
                    builder.like(builder.lower(root.get("name")), search, '\\'),
                    builder.like(builder.lower(root.get("slug")), search, '\\')
                ));
            }
            if (filter.categoryId() != null) {
                predicates.add(builder.equal(root.get("categoryId"), filter.categoryId()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static Category requiredCategory(Map<UUID, Category> categories, UUID categoryId) {
        var category = categories.get(categoryId);
        if (category == null) {
            throw new IllegalStateException("Visible product category was not loaded");
        }
        return category;
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
