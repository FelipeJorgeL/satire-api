package br.com.api.satireapi.domain.inventory.internal.usecase.sale;

import br.com.api.satireapi.domain.catalog.ProductVariationNotFoundException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockCapacityExceededException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.catalog.ProductVariationStockUnavailableException;
import br.com.api.satireapi.domain.inventory.StockSaleGateway;
import br.com.api.satireapi.domain.inventory.StockSaleLine;
import br.com.api.satireapi.domain.inventory.StockSaleUnavailableException;
import br.com.api.satireapi.domain.inventory.StockSaleVariationNotFoundException;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaStockSaleGateway implements StockSaleGateway {

    private final ProductVariationStockGateway variationStockGateway;
    private final StockMovementRepository movementRepository;

    JpaStockSaleGateway(
        ProductVariationStockGateway variationStockGateway,
        StockMovementRepository movementRepository
    ) {
        this.variationStockGateway = variationStockGateway;
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional
    public void registerSale(UUID orderId, UUID customerId, List<StockSaleLine> lines) {
        if (orderId == null || customerId == null || lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Invalid stock sale data");
        }
        Set<UUID> variationIds = new HashSet<>();
        for (var line : lines) {
            if (!variationIds.add(line.variationId())) {
                throw new IllegalArgumentException("A variation cannot repeat in the order");
            }
            var adjustment = adjust(line);
            if (!adjustment.active()) {
                throw new StockSaleUnavailableException();
            }
            movementRepository.save(StockMovement.sale(
                line.variationId(), orderId, customerId, line.quantity(),
                adjustment.previousStock(), adjustment.newStock(),
                "Sale for order " + orderId
            ));
        }
    }

    private ProductVariationStockAdjustment adjust(StockSaleLine line) {
        try {
            return variationStockGateway.adjustStock(
                line.variationId(), Math.negateExact(line.quantity())
            );
        } catch (ProductVariationNotFoundException ex) {
            throw new StockSaleVariationNotFoundException();
        } catch (
            ProductVariationStockUnavailableException
            | ProductVariationStockCapacityExceededException ex
        ) {
            throw new StockSaleUnavailableException();
        }
    }
}
