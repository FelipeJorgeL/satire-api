package br.com.api.satireapi.domain.catalog.internal.usecase.favorite;

import br.com.api.satireapi.domain.catalog.internal.dto.response.FavoriteResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.FavoriteRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListFavoritesUseCase {

    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;

    public ListFavoritesUseCase(
        FavoriteRepository favoriteRepository,
        ProductRepository productRepository,
        CategoryRepository categoryRepository,
        ProductVariationRepository variationRepository,
        ProductImageRepository imageRepository
    ) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.variationRepository = variationRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public Page<FavoriteResponse> execute(UUID customerId, Pageable pageable) {
        var favorites = favoriteRepository.findVisibleByCustomerId(customerId, pageable);
        if (favorites.isEmpty()) {
            return Page.empty(pageable);
        }

        var productIds = favorites.stream().map(favorite -> favorite.getProductId()).toList();
        var products = productRepository.findAllByIdInAndActiveTrue(productIds).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));
        var categoryIds = products.values().stream().map(Product::getCategoryId).distinct().toList();
        var categories = categoryRepository.findAllByIdInAndActiveTrue(categoryIds).stream()
            .collect(Collectors.toMap(Category::getId, Function.identity()));
        var variations = variationRepository
            .findAllByProductIdInAndActiveTrueOrderByCreatedAtAsc(productIds).stream()
            .collect(Collectors.groupingBy(ProductVariation::getProductId));
        var images = imageRepository
            .findAllByProductIdInOrderByProductIdAscDisplayOrderAsc(productIds).stream()
            .collect(Collectors.groupingBy(ProductImage::getProductId));

        return favorites.map(favorite -> {
            var product = required(products, favorite.getProductId(), "product");
            var category = required(categories, product.getCategoryId(), "category");
            return new FavoriteResponse(
                favorite.getCreatedAt(),
                PublicCatalogMapper.toListItem(
                    product,
                    category,
                    variations.getOrDefault(product.getId(), List.of()),
                    images.getOrDefault(product.getId(), List.of())
                )
            );
        });
    }

    private static <T> T required(Map<UUID, T> values, UUID id, String type) {
        var value = values.get(id);
        if (value == null) {
            throw new IllegalStateException("Visible favorite " + type + " was not loaded");
        }
        return value;
    }
}
