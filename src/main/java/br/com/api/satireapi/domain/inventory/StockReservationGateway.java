package br.com.api.satireapi.domain.inventory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface StockReservationGateway {

    void reserve(
        UUID orderId,
        UUID customerId,
        List<StockReservationLine> lines,
        Instant expiresAt
    );

    void release(UUID orderId);

    void confirm(UUID orderId);

    int expire(Instant now, int limit);
}
