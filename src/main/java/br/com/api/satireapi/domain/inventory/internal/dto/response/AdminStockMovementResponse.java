package br.com.api.satireapi.domain.inventory.internal.dto.response;

import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminStockMovementResponse(
    UUID id,
    UUID variationId,
    UUID userId,
    StockMovementType type,
    int quantity,
    int previousStock,
    int newStock,
    String observation,
    OffsetDateTime createdAt
) {
}
