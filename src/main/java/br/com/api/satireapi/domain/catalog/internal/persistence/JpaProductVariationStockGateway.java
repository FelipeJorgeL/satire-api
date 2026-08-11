package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.ProductVariationNotFoundException;
import br.com.api.satireapi.domain.catalog.ProductVariationStock;
import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaProductVariationStockGateway implements ProductVariationStockGateway {

    private final ProductVariationRepository repository;

    JpaProductVariationStockGateway(ProductVariationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProductVariationStock> findById(UUID variationId) {
        return repository.findById(variationId).map(this::toStock);
    }

    @Override
    public Optional<ProductVariationStock> findPublicAvailability(
        UUID productId,
        UUID variationId
    ) {
        return repository.findPublicAvailability(productId, variationId).map(this::toStock);
    }

    @Override
    public Optional<ProductVariationStock> findByIdForUpdate(UUID variationId) {
        return repository.findByIdForStockUpdate(variationId).map(this::toStock);
    }

    private ProductVariationStock toStock(
        br.com.api.satireapi.domain.catalog.internal.model.ProductVariation variation
    ) {
        return new ProductVariationStock(
            variation.getId(),
            variation.getProductId(),
            variation.getSku(),
            variation.getName(),
            variation.getStock(),
            variation.isActive()
        );
    }

    @Override
    public ProductVariationStockAdjustment adjustStock(UUID variationId, int delta) {
        var variation = repository.findByIdForStockUpdate(variationId)
            .orElseThrow(ProductVariationNotFoundException::new);
        var previousStock = variation.getStock();
        var newStock = variation.adjustStock(delta);
        return new ProductVariationStockAdjustment(
            variation.getId(),
            variation.getProductId(),
            variation.getSku(),
            variation.getName(),
            previousStock,
            newStock,
            variation.isActive()
        );
    }
}
