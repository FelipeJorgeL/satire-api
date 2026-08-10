package br.com.api.satireapi.domain.inventory.internal.usecase.sale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.inventory.StockSaleLine;
import br.com.api.satireapi.domain.inventory.StockSaleUnavailableException;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JpaStockSaleGatewayTest {

    private final ProductVariationStockGateway variationGateway =
        mock(ProductVariationStockGateway.class);
    private final StockMovementRepository movementRepository = mock(StockMovementRepository.class);
    private final JpaStockSaleGateway gateway = new JpaStockSaleGateway(
        variationGateway, movementRepository
    );

    @Test
    void registersSaleMovementAndDecreasesStock() {
        var variationId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        when(variationGateway.adjustStock(variationId, -2)).thenReturn(
            new ProductVariationStockAdjustment(
                variationId, UUID.randomUUID(), "SKU-1", "Variation", 5, 3, true
            )
        );
        when(movementRepository.save(any(StockMovement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        gateway.registerSale(orderId, customerId, List.of(new StockSaleLine(variationId, 2)));

        var captor = ArgumentCaptor.forClass(StockMovement.class);
        org.mockito.Mockito.verify(movementRepository).save(captor.capture());
        assertEquals(StockMovementType.VENDA, captor.getValue().getType());
    }

    @Test
    void rejectsInactiveVariation() {
        var variationId = UUID.randomUUID();
        when(variationGateway.adjustStock(variationId, -1)).thenReturn(
            new ProductVariationStockAdjustment(
                variationId, UUID.randomUUID(), "SKU-1", "Variation", 5, 4, false
            )
        );

        assertThrows(
            StockSaleUnavailableException.class,
            () -> gateway.registerSale(
                UUID.randomUUID(), UUID.randomUUID(), List.of(new StockSaleLine(variationId, 1))
            )
        );
    }
}
