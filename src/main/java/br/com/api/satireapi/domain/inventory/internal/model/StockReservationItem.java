package br.com.api.satireapi.domain.inventory.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "itens_reservas_estoque")
public class StockReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private StockReservation reservation;

    @Column(name = "variacao_produto_id", nullable = false)
    private UUID variationId;

    @Column(name = "quantidade", nullable = false)
    private int quantity;

    protected StockReservationItem() {
    }

    private StockReservationItem(StockReservation reservation, UUID variationId, int quantity) {
        this.reservation = reservation;
        this.variationId = variationId;
        this.quantity = quantity;
    }

    static StockReservationItem create(
        StockReservation reservation,
        UUID variationId,
        int quantity
    ) {
        return new StockReservationItem(reservation, variationId, quantity);
    }

    public UUID getVariationId() {
        return variationId;
    }

    public int getQuantity() {
        return quantity;
    }
}
