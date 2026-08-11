package br.com.api.satireapi.domain.inventory.internal.web;

import br.com.api.satireapi.domain.inventory.internal.dto.response.ProductVariationAvailabilityResponse;
import br.com.api.satireapi.domain.inventory.internal.usecase.variation.GetProductVariationAvailabilityUseCase;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/{productId}/variations")
class ProductVariationAvailabilityController {

    private final GetProductVariationAvailabilityUseCase availabilityUseCase;

    ProductVariationAvailabilityController(
        GetProductVariationAvailabilityUseCase availabilityUseCase
    ) {
        this.availabilityUseCase = availabilityUseCase;
    }

    @GetMapping("/{variationId}/availability")
    ProductVariationAvailabilityResponse get(
        @PathVariable UUID productId,
        @PathVariable UUID variationId
    ) {
        return availabilityUseCase.execute(productId, variationId);
    }
}
