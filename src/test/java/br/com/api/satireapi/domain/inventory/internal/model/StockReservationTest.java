package br.com.api.satireapi.domain.inventory.internal.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.api.satireapi.domain.inventory.StockReservationLine;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockReservationTest {

    @Test
    void doesNotAllowConfirmingReleasedReservation() {
        var reservation = StockReservation.create(
            UUID.randomUUID(), UUID.randomUUID(),
            List.of(new StockReservationLine(UUID.randomUUID(), 1)),
            Instant.now().plusSeconds(60)
        );

        reservation.release(false);

        assertThrows(IllegalStateException.class, reservation::confirm);
        assertEquals(StockReservationStatus.RELEASED, reservation.getStatus());
    }
}
