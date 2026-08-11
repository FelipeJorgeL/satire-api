package br.com.api.satireapi.domain.inventory.internal.usecase.reservation;

import br.com.api.satireapi.domain.catalog.ProductVariationNotFoundException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockAdjustment;
import br.com.api.satireapi.domain.catalog.ProductVariationStockCapacityExceededException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockGateway;
import br.com.api.satireapi.domain.catalog.ProductVariationStockUnavailableException;
import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import br.com.api.satireapi.domain.inventory.StockReservationLine;
import br.com.api.satireapi.domain.inventory.StockReservationUnavailableException;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservation;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservationItem;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservationStatus;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockReservationRepository;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaStockReservationGateway implements StockReservationGateway {

    private final ProductVariationStockGateway variationStockGateway;
    private final StockReservationRepository reservationRepository;
    private final StockMovementRepository movementRepository;

    JpaStockReservationGateway(
        ProductVariationStockGateway variationStockGateway,
        StockReservationRepository reservationRepository,
        StockMovementRepository movementRepository
    ) {
        this.variationStockGateway = variationStockGateway;
        this.reservationRepository = reservationRepository;
        this.movementRepository = movementRepository;
    }

    @Override
    @Transactional
    public void reserve(
        UUID orderId,
        UUID customerId,
        List<StockReservationLine> lines,
        Instant expiresAt
    ) {
        validate(orderId, customerId, lines, expiresAt);
        var existing = reservationRepository.findByOrderIdForUpdate(orderId);
        if (existing.isPresent()) {
            if (existing.get().getStatus() == StockReservationStatus.ACTIVE
                || existing.get().getStatus() == StockReservationStatus.CONFIRMED) {
                return;
            }
            throw new StockReservationUnavailableException();
        }

        var reservation = StockReservation.create(orderId, customerId, lines, expiresAt);
        for (var line : lines) {
            var adjustment = adjust(line, -line.quantity());
            if (!adjustment.active()) {
                throw new StockReservationUnavailableException();
            }
            movementRepository.save(StockMovement.reservation(
                line.variationId(), orderId, customerId, line.quantity(),
                adjustment.previousStock(), adjustment.newStock(),
                "Stock reserved for order " + orderId
            ));
        }
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public void release(UUID orderId) {
        reservationRepository.findByOrderIdForUpdate(orderId)
            .ifPresent(reservation -> releaseReservation(reservation, false));
    }

    @Override
    @Transactional
    public void confirm(UUID orderId) {
        reservationRepository.findByOrderIdForUpdate(orderId).ifPresent(reservation -> {
            if (reservation.getStatus() == StockReservationStatus.CONFIRMED) {
                return;
            }
            if (reservation.getStatus() != StockReservationStatus.ACTIVE) {
                throw new StockReservationUnavailableException();
            }
            reservation.getItems().forEach(item -> movementRepository.save(StockMovement.sale(
                item.getVariationId(), orderId, reservation.getCustomerId(), item.getQuantity(),
                currentStock(item.getVariationId()), currentStock(item.getVariationId()),
                "Stock sale confirmed for order " + orderId
            )));
            reservation.confirm();
        });
    }

    @Override
    @Transactional
    public int expire(Instant now, int limit) {
        if (now == null || limit <= 0) {
            throw new IllegalArgumentException("Invalid reservation expiration request");
        }
        var reservations = reservationRepository.findExpiredForUpdate(
            StockReservationStatus.ACTIVE,
            OffsetDateTime.ofInstant(now, ZoneOffset.UTC)
        );
        var expired = 0;
        for (var reservation : reservations.stream().limit(limit).toList()) {
            releaseReservation(reservation, true);
            expired++;
        }
        return expired;
    }

    private void releaseReservation(StockReservation reservation, boolean expired) {
        if (reservation.getStatus() != StockReservationStatus.ACTIVE) {
            return;
        }
        for (var item : reservation.getItems()) {
            var adjustment = adjust(item, item.getQuantity());
            movementRepository.save(StockMovement.release(
                item.getVariationId(), reservation.getOrderId(), reservation.getCustomerId(),
                item.getQuantity(), adjustment.previousStock(), adjustment.newStock(),
                (expired ? "Expired" : "Cancelled")
                    + " stock reservation for order " + reservation.getOrderId()
            ));
        }
        reservation.release(expired);
    }

    private ProductVariationStockAdjustment adjust(StockReservationLine line, int delta) {
        return adjust(line.variationId(), delta);
    }

    private ProductVariationStockAdjustment adjust(StockReservationItem item, int delta) {
        return adjust(item.getVariationId(), delta);
    }

    private ProductVariationStockAdjustment adjust(UUID variationId, int delta) {
        try {
            return variationStockGateway.adjustStock(variationId, delta);
        } catch (
            ProductVariationNotFoundException
            | ProductVariationStockUnavailableException
            | ProductVariationStockCapacityExceededException ex
        ) {
            throw new StockReservationUnavailableException();
        }
    }

    private int currentStock(UUID variationId) {
        return variationStockGateway.findByIdForUpdate(variationId)
            .orElseThrow(StockReservationUnavailableException::new)
            .stock();
    }

    private static void validate(
        UUID orderId,
        UUID customerId,
        List<StockReservationLine> lines,
        Instant expiresAt
    ) {
        if (orderId == null || customerId == null || lines == null || lines.isEmpty()
            || expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Invalid stock reservation data");
        }
        var variationIds = new HashSet<UUID>();
        for (var line : lines) {
            if (!variationIds.add(line.variationId())) {
                throw new IllegalArgumentException("A variation cannot repeat in a reservation");
            }
        }
    }
}
