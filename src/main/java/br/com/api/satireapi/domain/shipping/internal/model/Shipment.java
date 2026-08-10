package br.com.api.satireapi.domain.shipping.internal.model;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "entregas")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "transportadora", length = 100)
    private String carrier;

    @Column(name = "codigo_rastreio", unique = true, length = 120)
    private String trackingCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ShippingStatus status;

    @Column(name = "enviado_em")
    private OffsetDateTime sentAt;

    @Column(name = "entregue_em")
    private OffsetDateTime deliveredAt;

    @Column(name = "previsao_entrega")
    private LocalDate estimatedDelivery;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected Shipment() {
    }

    private Shipment(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("O pedido da entrega Ã© obrigatÃ³rio");
        }
        this.orderId = orderId;
        this.status = ShippingStatus.AGUARDANDO_ENVIO;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Shipment awaitingForOrder(UUID orderId) {
        return new Shipment(orderId);
    }

    public static Shipment awaitingForOrder(UUID orderId, LocalDate estimatedDelivery) {
        var shipment = new Shipment(orderId);
        shipment.estimatedDelivery = estimatedDelivery;
        return shipment;
    }

    public void updateDetails(
        String carrier,
        String trackingCode,
        LocalDate estimatedDelivery
    ) {
        if (isTerminal()) {
            throw new ShipmentDetailsNotEditableException();
        }
        if (carrier != null) {
            this.carrier = normalize(carrier);
        }
        if (trackingCode != null) {
            this.trackingCode = normalize(trackingCode);
        }
        if (estimatedDelivery != null) {
            this.estimatedDelivery = estimatedDelivery;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeStatus(ShippingStatus target) {
        if (!ShippingStatusTransitionPolicy.allows(status, target)) {
            throw new InvalidShippingStatusTransitionException(status, target);
        }
        var now = OffsetDateTime.now();
        if (target == ShippingStatus.ENVIADO && sentAt == null) {
            sentAt = now;
        }
        if (target == ShippingStatus.ENTREGUE && deliveredAt == null) {
            deliveredAt = now;
        }
        status = target;
        updatedAt = now;
    }

    private boolean isTerminal() {
        return status == ShippingStatus.ENTREGUE || status == ShippingStatus.DEVOLVIDO;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getTrackingCode() {
        return trackingCode;
    }

    public ShippingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public OffsetDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public LocalDate getEstimatedDelivery() {
        return estimatedDelivery;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
