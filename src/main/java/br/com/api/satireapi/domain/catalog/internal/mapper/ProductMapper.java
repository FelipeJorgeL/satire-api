package br.com.api.satireapi.domain.catalog.internal.mapper;

import br.com.api.satireapi.domain.catalog.dto.*;
import br.com.api.satireapi.domain.catalog.internal.dto.response.ProductResponse;
import br.com.api.satireapi.domain.catalog.internal.model.*;

public final class ProductMapper {

    private ProductMapper() {
    }

    public ProductImageSummary productImageToSummary(ProductImage image) {
        return new ProductImageSummary(
                image.getId(),
                image.getUrl(),
                image.getAlternativeText(),
                image.isMainImage(),
                image.getDisplayOrder()
        );
    }

    public ProductVariationSummary productVariationToSummary(ProductVariation variation) {
        return new ProductVariationSummary(
                variation.getId(),
                variation.getName(),
                variation.getSku(),
                variation.getPrice(),
                variation.getInventory(),
                variation.isActive()
        );
    }

    public ProductResponse productToResponse(Product product) {
        var images = product.getProductImages().stream()
                .map(this::productImageToSummary)
                .toList();

        var variations = product.getProductVariations().stream()
                .map(this::productVariationToSummary)
                .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.isActive(),
                this.categoryToSummary(product.getCategory()),
                images,
                variations
        );
    }

    public ProductResponse summaryToResponse(ProductSummary summary) {
        return new ProductResponse(
                summary.id(),
                summary.name(),
                summary.slug(),
                summary.description(),
                summary.active(),
                summary.category(),
                summary.images(),
                summary.variations()
        );
    }

    public ProductSummary productToSummary(Product product) {
        return new ProductSummary(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.isActive(),
                this.categoryToSummary(product.getCategory()),
                product.getProductImages()
                        .stream()
                        .map(this::productImageToSummary)
                        .toList(),
                product.getProductVariations()
                        .stream()
                        .map(this::productVariationToSummary)
                        .toList()
        );
    }
}