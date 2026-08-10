package br.com.api.satireapi.domain.inventory.internal.dto.response;

import java.util.List;
import java.util.UUID;

public record AdminStockVariationResponse(
    UUID variationId,
    UUID productId,
    String sku,
    String name,
    boolean active,
    int currentStock,
    long totalEntryQuantity,
    long totalExitQuantity,
    List<AdminStockMovementResponse> recentMovements
) {
}
