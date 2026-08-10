package br.com.api.satireapi.domain.inventory.internal.dto.request;

import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminStockMovementFilter(
    UUID variationId,
    StockMovementType type,
    OffsetDateTime from,
    OffsetDateTime to
) {
}
