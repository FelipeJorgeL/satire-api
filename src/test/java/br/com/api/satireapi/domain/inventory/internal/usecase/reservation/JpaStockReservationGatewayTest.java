package br.com.api.satireapi.domain.inventory.internal.usecase.reservation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.inventory.StockReservationLine;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservation;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservationStatus;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockReservationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JpaStockReservationGatewayTest {

    private final ProductVariationStockGateway variationGateway = mock(ProductVariationStockGateway.class);
    private final StockReservationRepository reservationRepository = mock(StockReservationRepository.class);
    private final StockMovementRepository movementRepository = mock(StockMovementRepository.class);
    private final JpaStockReservationGateway gateway = new JpaStockReservationGateway(
        variationGateway, reservationRepository, movementRepository
    );

    @Test
    void reservesStockAndIsIdempotentForAnActiveOrder() {
        var orderId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var adjustment = new ProductVariationStockAdjustment(
            variationId, UUID.randomUUID(), "SKU-1", "Variation", 5, 3, true
        );
        when(reservationRepository.findByOrderIdForUpdate(orderId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(StockReservation.create(
                orderId, customerId,
                List.of(new StockReservationLine(variationId, 2)),
                Instant.now().plusSeconds(60)
            )));
        when(variationGateway.adjustStock(variationId, -2)).thenReturn(adjustment);

        gateway.reserve(
            orderId, customerId, List.of(new StockReservationLine(variationId, 2)),
            Instant.now().plusSeconds(60)
        );
        gateway.reserve(
            orderId, customerId, List.of(new StockReservationLine(variationId, 2)),
            Instant.now().plusSeconds(60)
        );

        verify(variationGateway).adjustStock(variationId, -2);
        verify(reservationRepository).save(any(StockReservation.class));
    }

    @Test
    void releasesActiveReservationOnlyOnce() {
        var orderId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var reservation = StockReservation.create(
            orderId, customerId,
            List.of(new StockReservationLine(variationId, 2)),
            Instant.now().plusSeconds(60)
        );
        when(reservationRepository.findByOrderIdForUpdate(orderId))
            .thenReturn(Optional.of(reservation));
        when(variationGateway.adjustStock(variationId, 2)).thenReturn(
            new ProductVariationStockAdjustment(
                variationId, UUID.randomUUID(), "SKU-1", "Variation", 3, 5, true
            )
        );

        gateway.release(orderId);
        gateway.release(orderId);

        assertEquals(StockReservationStatus.RELEASED, reservation.getStatus());
        verify(variationGateway).adjustStock(variationId, 2);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void confirmsActiveReservationWithoutChangingStock() {
        var orderId = UUID.randomUUID();
        var customerId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var reservation = StockReservation.create(
            orderId, customerId,
            List.of(new StockReservationLine(variationId, 2)),
            Instant.now().plusSeconds(60)
        );
        when(reservationRepository.findByOrderIdForUpdate(orderId))
            .thenReturn(Optional.of(reservation));
        when(variationGateway.findByIdForUpdate(variationId)).thenReturn(Optional.of(
            new br.com.api.satireapi.domain.catalog.ProductVariationStock(
                variationId, UUID.randomUUID(), "SKU-1", "Variation", 3, true
            )
        ));

        gateway.confirm(orderId);

        assertEquals(StockReservationStatus.CONFIRMED, reservation.getStatus());
        verify(variationGateway, never()).adjustStock(any(), any(Integer.class));
        verify(movementRepository).save(any(StockMovement.class));
    }
}
