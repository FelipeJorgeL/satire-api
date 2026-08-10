package br.com.api.satireapi.domain.order.internal.model;

import br.com.api.satireapi.domain.order.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "historicos_status_pedidos")
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID orderId;

    @Column(name = "usuario_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 30)
    private OrderStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 30)
    private OrderStatus newStatus;

    @Column(name = "motivo", length = 255)
    private String reason;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    protected OrderStatusHistory() {
    }

    private OrderStatusHistory(
        UUID orderId,
        UUID userId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String reason
    ) {
        if (orderId == null || userId == null || newStatus == null
            || (previousStatus == null && newStatus != OrderStatus.AGUARDANDO_PAGAMENTO)
            || (previousStatus != null && previousStatus == newStatus)) {
            throw new IllegalArgumentException("Histórico de status inválido");
        }
        this.orderId = orderId;
        this.userId = userId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = reason == null || reason.isBlank() ? null : reason.trim();
        this.createdAt = OffsetDateTime.now();
    }

    public static OrderStatusHistory record(
        UUID orderId,
        UUID userId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String reason
    ) {
        return new OrderStatusHistory(orderId, userId, previousStatus, newStatus, reason);
    }

    public static OrderStatusHistory initial(UUID orderId, UUID userId) {
        return new OrderStatusHistory(
            orderId, userId, null, OrderStatus.AGUARDANDO_PAGAMENTO, null
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getUserId() {
        return userId;
    }

    public OrderStatus getPreviousStatus() {
        return previousStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public String getReason() {
        return reason;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
