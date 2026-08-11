package br.com.api.satireapi.domain.catalog.internal.mapper;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import java.util.Comparator;
import java.util.List;

public final class PublicCatalogMapper {

    private PublicCatalogMapper() {
    }

    public static CategorySummary toCategory(Category category) {
        return new CategorySummary(category.getId(), category.getName(), category.getSlug());
    }

    public static ProductVariationSummary toVariation(ProductVariation variation) {
        return new ProductVariationSummary(
            variation.getId(), variation.getName(), variation.getSku(), variation.getPrice(),
            variation.getStock(), variation.isActive()
        );
    }

    public static ProductImageSummary toImage(ProductImage image) {
        return new ProductImageSummary(
            image.getId(), image.getUrl(), image.getAltText(), image.isPrimary(),
            image.getDisplayOrder()
        );
    }

    public static PublicProductListItemResponse toListItem(
        Product product,
        Category category,
        List<ProductVariation> variations,
        List<ProductImage> images
    ) {
        var minimumPrice = variations.stream()
            .map(ProductVariation::getPrice)
            .min(Comparator.naturalOrder())
            .orElse(null);
        var primaryImage = images.stream()
            .filter(ProductImage::isPrimary)
            .findFirst()
            .map(PublicCatalogMapper::toImage)
            .orElse(null);
        return new PublicProductListItemResponse(
            product.getId(), product.getName(), product.getSlug(), product.getDescription(),
            toCategory(category), minimumPrice, primaryImage
        );
    }

    public static PublicProductDetailsResponse toDetails(
        Product product,
        Category category,
        List<ProductVariation> variations,
        List<ProductImage> images
    ) {
        return new PublicProductDetailsResponse(
            product.getId(), product.getName(), product.getSlug(), product.getDescription(),
            toCategory(category), images.stream().map(PublicCatalogMapper::toImage).toList(),
            variations.stream().map(PublicCatalogMapper::toVariation).toList()
        );
    }
}
