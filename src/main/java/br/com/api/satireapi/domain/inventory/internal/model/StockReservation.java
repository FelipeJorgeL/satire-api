package br.com.api.satireapi.domain.inventory.internal.model;

import br.com.api.satireapi.domain.inventory.StockReservationLine;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservas_estoque")
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "usuario_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockReservationStatus status;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
        mappedBy = "reservation",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<StockReservationItem> items = new ArrayList<>();

    protected StockReservation() {
    }

    private StockReservation(UUID orderId, UUID customerId, Instant expiresAt) {
        if (orderId == null || customerId == null || expiresAt == null) {
            throw new IllegalArgumentException("Invalid stock reservation");
        }
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = StockReservationStatus.ACTIVE;
        this.expiresAt = OffsetDateTime.ofInstant(expiresAt, java.time.ZoneOffset.UTC);
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static StockReservation create(
        UUID orderId,
        UUID customerId,
        List<StockReservationLine> lines,
        Instant expiresAt
    ) {
        var reservation = new StockReservation(orderId, customerId, expiresAt);
        lines.forEach(line -> reservation.items.add(
            StockReservationItem.create(reservation, line.variationId(), line.quantity())
        ));
        return reservation;
    }

    public void release(boolean expired) {
        if (status != StockReservationStatus.ACTIVE) {
            return;
        }
        status = expired ? StockReservationStatus.EXPIRED : StockReservationStatus.RELEASED;
        updatedAt = OffsetDateTime.now();
    }

    public void confirm() {
        if (status == StockReservationStatus.CONFIRMED) {
            return;
        }
        if (status != StockReservationStatus.ACTIVE) {
            throw new IllegalStateException("Only active reservations can be confirmed");
        }
        status = StockReservationStatus.CONFIRMED;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public StockReservationStatus getStatus() {
        return status;
    }

    public List<StockReservationItem> getItems() {
        return items;
    }
}
