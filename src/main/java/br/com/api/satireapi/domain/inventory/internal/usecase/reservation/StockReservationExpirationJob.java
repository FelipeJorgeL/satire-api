package br.com.api.satireapi.domain.inventory.internal.usecase.reservation;

import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class StockReservationExpirationJob {

    private static final int BATCH_SIZE = 100;

    private final StockReservationGateway reservationGateway;

    StockReservationExpirationJob(StockReservationGateway reservationGateway) {
        this.reservationGateway = reservationGateway;
    }

    @Scheduled(fixedDelayString = "${app.inventory.reservation-expiration-interval:PT1M}")
    void expireReservations() {
        reservationGateway.expire(Instant.now(), BATCH_SIZE);
    }
}
