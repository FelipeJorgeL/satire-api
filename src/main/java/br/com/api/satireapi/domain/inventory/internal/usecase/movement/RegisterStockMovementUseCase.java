package br.com.api.satireapi.domain.inventory.internal.usecase.movement;

import br.com.api.satireapi.domain.catalog.ProductVariationNotFoundException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockCapacityExceededException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockUnavailableException;
import br.com.api.satireapi.domain.inventory.internal.dto.request.RegisterStockMovementRequest;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockMovementResponse;
import br.com.api.satireapi.domain.inventory.internal.mapper.InventoryMapper;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.usecase.InsufficientStockException;
import br.com.api.satireapi.domain.inventory.internal.usecase.StockCapacityExceededException;
import br.com.api.satireapi.domain.inventory.internal.usecase.StockVariationNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterStockMovementUseCase {

    private final ProductVariationStockGateway variationStockGateway;
    private final StockMovementRepository movementRepository;

    public RegisterStockMovementUseCase(
        ProductVariationStockGateway variationStockGateway,
        StockMovementRepository movementRepository
    ) {
        this.variationStockGateway = variationStockGateway;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public AdminStockMovementResponse execute(
        UUID adminId,
        RegisterStockMovementRequest request
    ) {
        var adjustment = adjustStock(request);
        var movement = StockMovement.manual(
            adjustment.variationId(),
            adminId,
            request.type(),
            request.quantity(),
            adjustment.previousStock(),
            adjustment.newStock(),
            request.observation()
        );
        return InventoryMapper.toResponse(movementRepository.save(movement));
    }

    private ProductVariationStockAdjustment adjustStock(
        RegisterStockMovementRequest request
    ) {
        try {
            return variationStockGateway.adjustStock(
                request.variationId(), request.type().delta(request.quantity())
            );
        } catch (ProductVariationNotFoundException ex) {
            throw new StockVariationNotFoundException();
        } catch (ProductVariationStockUnavailableException ex) {
            throw new InsufficientStockException();
        } catch (ProductVariationStockCapacityExceededException ex) {
            throw new StockCapacityExceededException();
        }
    }
}
