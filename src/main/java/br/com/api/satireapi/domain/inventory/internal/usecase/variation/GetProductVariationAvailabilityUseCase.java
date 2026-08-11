package br.com.api.satireapi.domain.inventory.internal.usecase.variation;

import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.inventory.internal.dto.response.ProductVariationAvailabilityResponse;
import br.com.api.satireapi.domain.inventory.internal.usecase.StockVariationNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetProductVariationAvailabilityUseCase {

    private final ProductVariationStockGateway variationStockGateway;

    public GetProductVariationAvailabilityUseCase(
        ProductVariationStockGateway variationStockGateway
    ) {
        this.variationStockGateway = variationStockGateway;
    }

    @Transactional(readOnly = true)
    public ProductVariationAvailabilityResponse execute(UUID productId, UUID variationId) {
        var variation = variationStockGateway.findPublicAvailability(productId, variationId)
            .orElseThrow(StockVariationNotFoundException::new);
        return new ProductVariationAvailabilityResponse(
            variation.productId(),
            variation.variationId(),
            variation.stock() > 0,
            variation.stock()
        );
    }
}
