package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.ProductVariationPurchase;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchaseGateway;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaProductVariationPurchaseGateway implements ProductVariationPurchaseGateway {

    private final ProductVariationRepository variationRepository;
    private final ProductRepository productRepository;

    JpaProductVariationPurchaseGateway(
        ProductVariationRepository variationRepository,
        ProductRepository productRepository
    ) {
        this.variationRepository = variationRepository;
        this.productRepository = productRepository;
    }

    @Override
    public Optional<ProductVariationPurchase> findById(UUID variationId) {
        return variationRepository.findById(variationId)
            .flatMap(variation -> productRepository.findById(variation.getProductId())
                .map(product -> new ProductVariationPurchase(
                    variation.getId(), variation.getProductId(), product.getName(),
                    variation.getSku(), variation.getName(), variation.getPrice(),
                    variation.getStock(), variation.isActive() && product.isActive()
                )));
    }
}
