package br.com.api.satireapi.domain.catalog.internal.mapper;

import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminCategoryResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductImageResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductVariationResponse;
import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import java.util.List;

public final class CatalogMapper {

    private CatalogMapper() {
    }

    public static AdminCategoryResponse toResponse(Category category) {
        return new AdminCategoryResponse(
            category.getId(), category.getName(), category.getSlug(), category.isActive()
        );
    }

    public static AdminProductListItemResponse toListItem(Product product) {
        return new AdminProductListItemResponse(
            product.getId(), product.getCategoryId(), product.getName(), product.getSlug(),
            product.isActive(), product.getCreatedAt()
        );
    }

    public static AdminProductDetailsResponse toDetails(
        Product product,
        List<ProductVariation> variations,
        List<ProductImage> images
    ) {
        return new AdminProductDetailsResponse(
            product.getId(), product.getCategoryId(), product.getName(), product.getSlug(),
            product.getDescription(), product.isActive(), product.getCreatedAt(),
            variations.stream().map(CatalogMapper::toVariation).toList(),
            images.stream().map(CatalogMapper::toImage).toList()
        );
    }

    private static AdminProductVariationResponse toVariation(ProductVariation variation) {
        return new AdminProductVariationResponse(
            variation.getId(), variation.getSku(), variation.getName(), variation.getPrice(),
            variation.getStock(), variation.isActive()
        );
    }

    private static AdminProductImageResponse toImage(ProductImage image) {
        return new AdminProductImageResponse(
            image.getId(), image.getUrl(), image.getAltText(), image.isDecorative(),
            image.isPrimary(), image.getDisplayOrder()
        );
    }
}
