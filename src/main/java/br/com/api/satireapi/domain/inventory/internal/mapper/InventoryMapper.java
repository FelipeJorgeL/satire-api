package br.com.api.satireapi.domain.inventory.internal.mapper;

import br.com.api.satireapi.domain.catalog.ProductVariationStock;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockMovementResponse;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockVariationResponse;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import java.util.List;

public final class InventoryMapper {

    private InventoryMapper() {
    }

    public static AdminStockMovementResponse toResponse(StockMovement movement) {
        return new AdminStockMovementResponse(
            movement.getId(), movement.getVariationId(), movement.getUserId(), movement.getType(),
            movement.getQuantity(), movement.getPreviousStock(), movement.getNewStock(),
            movement.getObservation(), movement.getCreatedAt()
        );
    }

    public static AdminStockVariationResponse toVariationResponse(
        ProductVariationStock variation,
        long totalEntryQuantity,
        long totalExitQuantity,
        List<AdminStockMovementResponse> recentMovements
    ) {
        return new AdminStockVariationResponse(
            variation.variationId(), variation.productId(), variation.sku(), variation.name(),
            variation.active(), variation.stock(), totalEntryQuantity, totalExitQuantity,
            recentMovements
        );
    }
}
