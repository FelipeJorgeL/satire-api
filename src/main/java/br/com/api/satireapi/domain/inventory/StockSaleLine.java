package br.com.api.satireapi.domain.inventory;

import java.util.UUID;

public record StockSaleLine(UUID variationId, int quantity) {

    public StockSaleLine {
        if (variationId == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid stock sale item");
        }
    }
}
