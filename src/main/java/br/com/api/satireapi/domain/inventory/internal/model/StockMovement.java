package br.com.api.satireapi.domain.inventory.internal.model;

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
@Table(name = "movimentacoes_estoque")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "variacao_produto_id", nullable = false)
    private UUID variationId;

    @Column(name = "usuario_id")
    private UUID userId;

    @Column(name = "pedido_id")
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private StockMovementType type;

    @Column(name = "quantidade", nullable = false)
    private int quantity;

    @Column(name = "estoque_anterior", nullable = false)
    private int previousStock;

    @Column(name = "estoque_novo", nullable = false)
    private int newStock;

    @Column(name = "observacao", length = 255)
    private String observation;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    protected StockMovement() {
    }

    private StockMovement(
        UUID variationId,
        UUID userId,
        UUID orderId,
        StockMovementType type,
        int quantity,
        int previousStock,
        int newStock,
        String observation
    ) {
        if (variationId == null || userId == null || type == null || quantity <= 0) {
            throw new IllegalArgumentException("Dados da movimentação de estoque inválidos");
        }
        if (!isConsistent(type, quantity, previousStock, newStock)) {
            throw new IllegalArgumentException("Saldos incompatíveis com a movimentação");
        }

        this.variationId = variationId;
        this.userId = userId;
        this.orderId = orderId;
        this.type = type;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.observation = normalizeObservation(observation);
        this.createdAt = OffsetDateTime.now();
    }

    public static StockMovement manual(
        UUID variationId,
        UUID userId,
        StockMovementType type,
        int quantity,
        int previousStock,
        int newStock,
        String observation
    ) {
        if (type != StockMovementType.ENTRADA && type != StockMovementType.SAIDA) {
            throw new IllegalArgumentException("Somente movimentações manuais são permitidas");
        }
        return new StockMovement(
            variationId, userId, null, type, quantity, previousStock, newStock, observation
        );
    }

    public static StockMovement sale(
        UUID variationId,
        UUID orderId,
        UUID customerId,
        int quantity,
        int previousStock,
        int newStock,
        String observation
    ) {
        return new StockMovement(
            variationId, customerId, orderId, StockMovementType.VENDA,
            quantity, previousStock, newStock, observation
        );
    }

    private static boolean isConsistent(
        StockMovementType type,
        int quantity,
        int previousStock,
        int newStock
    ) {
        if (previousStock < 0 || newStock < 0) {
            return false;
        }
        return (long) newStock == (long) previousStock + type.delta(quantity);
    }

    private static String normalizeObservation(String observation) {
        if (observation == null) {
            return null;
        }
        var normalized = observation.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public UUID getId() {
        return id;
    }

    public UUID getVariationId() {
        return variationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public StockMovementType getType() {
        return type;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPreviousStock() {
        return previousStock;
    }

    public int getNewStock() {
        return newStock;
    }

    public String getObservation() {
        return observation;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
