package br.com.api.satireapi.domain.inventory.internal.usecase.variation;

import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockVariationResponse;
import br.com.api.satireapi.domain.inventory.internal.mapper.InventoryMapper;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.usecase.StockVariationNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminStockVariationUseCase {

    private final ProductVariationStockGateway variationStockGateway;
    private final StockMovementRepository movementRepository;

    public GetAdminStockVariationUseCase(
        ProductVariationStockGateway variationStockGateway,
        StockMovementRepository movementRepository
    ) {
        this.variationStockGateway = variationStockGateway;
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public AdminStockVariationResponse execute(UUID variationId) {
        var variation = variationStockGateway.findById(variationId)
            .orElseThrow(StockVariationNotFoundException::new);
        var recentMovements = movementRepository.findTop20ByVariationIdOrderByCreatedAtDesc(variationId)
            .stream()
            .map(InventoryMapper::toResponse)
            .toList();
        var totalEntries = movementRepository.sumQuantityByVariationIdAndType(
            variationId, StockMovementType.ENTRADA
        );
        var totalExits = movementRepository.sumQuantityByVariationIdAndType(
            variationId, StockMovementType.SAIDA
        );
        return InventoryMapper.toVariationResponse(
            variation, totalEntries, totalExits, recentMovements
        );
    }
}
