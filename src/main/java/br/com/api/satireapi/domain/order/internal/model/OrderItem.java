package br.com.api.satireapi.domain.order.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "itens_pedidos")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pedido_id", nullable = false)
    private UUID orderId;

    @Column(name = "variacao_produto_id")
    private UUID variationId;

    @Column(name = "produto_id")
    private UUID productId;

    @Column(name = "sku", nullable = false, length = 60)
    private String sku;

    @Column(name = "nome_produto", nullable = false, length = 180)
    private String productName;

    @Column(name = "nome_variacao", nullable = false, length = 120)
    private String variationName;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "quantidade", nullable = false)
    private int quantity;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected OrderItem() {
    }

    private OrderItem(
        UUID orderId,
        UUID variationId,
        UUID productId,
        String sku,
        String productName,
        String variationName,
        BigDecimal unitPrice,
        int quantity
    ) {
        if (orderId == null || variationId == null || productId == null || quantity <= 0
            || unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("Invalid order item");
        }
        this.orderId = orderId;
        this.variationId = variationId;
        this.productId = productId;
        this.sku = requireText(sku);
        this.productName = requireText(productName);
        this.variationName = requireText(variationName);
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static OrderItem create(
        UUID orderId,
        UUID variationId,
        UUID productId,
        String sku,
        String productName,
        String variationName,
        BigDecimal unitPrice,
        int quantity
    ) {
        return new OrderItem(
            orderId, variationId, productId, sku, productName, variationName,
            unitPrice, quantity
        );
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Order item text is required");
        }
        return value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getVariationId() {
        return variationId;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public String getVariationName() {
        return variationName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
