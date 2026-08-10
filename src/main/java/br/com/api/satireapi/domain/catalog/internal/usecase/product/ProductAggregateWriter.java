package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.dto.request.ProductRequest;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductVariationRepository;
import org.springframework.stereotype.Component;

@Component
class ProductAggregateWriter {

    private final ProductVariationRepository variationRepository;
    private final ProductImageRepository imageRepository;

    ProductAggregateWriter(
        ProductVariationRepository variationRepository,
        ProductImageRepository imageRepository
    ) {
        this.variationRepository = variationRepository;
        this.imageRepository = imageRepository;
    }

    void replaceChildren(Product product, ProductRequest request) {
        variationRepository.deleteAllByProductId(product.getId());
        imageRepository.deleteAllByProductId(product.getId());
        variationRepository.saveAll(request.variations().stream()
            .map(variation -> ProductVariation.create(
                product.getId(), variation.sku(), variation.name(), variation.price(), variation.stock()
            ))
            .toList());
        imageRepository.saveAll(request.images().stream()
            .map(image -> ProductImage.create(
                product.getId(), image.url(), image.decorative() ? null : image.altText(),
                image.primary(), image.displayOrder()
            ))
            .toList());
    }
}
