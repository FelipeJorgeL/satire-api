package br.com.api.satireapi.domain.inventory.internal.dto.request;

import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import java.util.UUID;

public record RegisterStockMovementRequest(
    @NotNull UUID variationId,
    @NotNull StockMovementType type,
    @NotNull @Positive @Max(1_000_000_000) Integer quantity,
    @Size(max = 255) String observation
) {

    @AssertTrue(message = "Manual movement must be ENTRADA or SAIDA")
    public boolean isManualType() {
        return type == StockMovementType.ENTRADA || type == StockMovementType.SAIDA;
    }
}
