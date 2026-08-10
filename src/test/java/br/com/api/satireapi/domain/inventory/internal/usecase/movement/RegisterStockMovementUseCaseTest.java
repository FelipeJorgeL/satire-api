package br.com.api.satireapi.domain.inventory.internal.usecase.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.catalog.ProductVariationStockUnavailableException;
import br.com.api.satireapi.domain.inventory.internal.dto.request.RegisterStockMovementRequest;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.usecase.InsufficientStockException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RegisterStockMovementUseCaseTest {

    private final ProductVariationStockGateway variationStockGateway = mock(ProductVariationStockGateway.class);
    private final StockMovementRepository movementRepository = mock(StockMovementRepository.class);
    private final RegisterStockMovementUseCase useCase = new RegisterStockMovementUseCase(
        variationStockGateway, movementRepository
    );

    @Test
    void registersEntryUsingTheAuthenticatedAdminAndNewBalance() {
        var adminId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        when(variationStockGateway.adjustStock(variationId, 5)).thenReturn(
            new ProductVariationStockAdjustment(
                variationId, UUID.randomUUID(), "SKU-1", "Variação", 10, 15, true
            )
        );
        when(movementRepository.save(any(StockMovement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(
            adminId,
            new RegisterStockMovementRequest(variationId, StockMovementType.ENTRADA, 5, "Reposição")
        );

        assertEquals(StockMovementType.ENTRADA, response.type());
        assertEquals(10, response.previousStock());
        assertEquals(15, response.newStock());
        assertEquals(adminId, response.userId());
        verify(variationStockGateway).adjustStock(variationId, 5);
    }

    @Test
    void registersExitWithNegativeDelta() {
        var variationId = UUID.randomUUID();
        when(variationStockGateway.adjustStock(variationId, -3)).thenReturn(
            new ProductVariationStockAdjustment(
                variationId, UUID.randomUUID(), "SKU-1", "Variação", 10, 7, true
            )
        );
        when(movementRepository.save(any(StockMovement.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(
            UUID.randomUUID(),
            new RegisterStockMovementRequest(variationId, StockMovementType.SAIDA, 3, null)
        );

        assertEquals(StockMovementType.SAIDA, response.type());
        assertEquals(7, response.newStock());
        verify(variationStockGateway).adjustStock(variationId, -3);
    }

    @Test
    void translatesCatalogInsufficientStockToInventoryConflict() {
        var variationId = UUID.randomUUID();
        when(variationStockGateway.adjustStock(eq(variationId), eq(-11)))
            .thenThrow(new ProductVariationStockUnavailableException());

        assertThrows(
            InsufficientStockException.class,
            () -> useCase.execute(
                UUID.randomUUID(),
                new RegisterStockMovementRequest(variationId, StockMovementType.SAIDA, 11, null)
            )
        );
    }
}
