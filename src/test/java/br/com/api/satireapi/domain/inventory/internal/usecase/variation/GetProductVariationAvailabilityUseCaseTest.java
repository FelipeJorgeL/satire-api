package br.com.api.satireapi.domain.inventory.internal.usecase.variation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.ProductVariationStock;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.inventory.internal.usecase.StockVariationNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetProductVariationAvailabilityUseCaseTest {

    private ProductVariationStockGateway stockGateway;
    private GetProductVariationAvailabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        stockGateway = mock(ProductVariationStockGateway.class);
        useCase = new GetProductVariationAvailabilityUseCase(stockGateway);
    }

    @Test
    void reportsAvailableQuantity() {
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        when(stockGateway.findPublicAvailability(productId, variationId)).thenReturn(Optional.of(
            new ProductVariationStock(variationId, productId, "SKU-1", "Default", 4, true)
        ));

        var result = useCase.execute(productId, variationId);

        assertEquals(productId, result.productId());
        assertEquals(variationId, result.variationId());
        assertEquals(4, result.availableQuantity());
        assertTrue(result.available());
    }

    @Test
    void reportsUnavailableWhenQuantityIsZero() {
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        when(stockGateway.findPublicAvailability(productId, variationId)).thenReturn(Optional.of(
            new ProductVariationStock(variationId, productId, "SKU-1", "Default", 0, true)
        ));

        var result = useCase.execute(productId, variationId);

        assertFalse(result.available());
        assertEquals(0, result.availableQuantity());
    }

    @Test
    void rejectsHiddenOrUnrelatedVariation() {
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        when(stockGateway.findPublicAvailability(productId, variationId)).thenReturn(Optional.empty());

        assertThrows(
            StockVariationNotFoundException.class,
            () -> useCase.execute(productId, variationId)
        );
    }
}
