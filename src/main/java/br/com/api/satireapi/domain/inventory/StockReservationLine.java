package br.com.api.satireapi.domain.inventory;

import java.util.UUID;

public record StockReservationLine(UUID variationId, int quantity) {

    public StockReservationLine {
        if (variationId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid stock reservation item");
        }
    }
}
