package br.com.api.satireapi.domain.order.internal.model;

import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.InvalidOrderStatusTransitionException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID customerId;

    @Column(name = "numero", nullable = false, unique = true, length = 30)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "desconto", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @Column(name = "frete", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "valor_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected Order() {
    }

    public static Order place(
        UUID customerId,
        String number,
        BigDecimal subtotal,
        BigDecimal shippingFee
    ) {
        requireAmount(subtotal);
        requireAmount(shippingFee);
        if (customerId == null || number == null || number.isBlank()) {
            throw new IllegalArgumentException("Order identity is required");
        }
        var order = new Order();
        order.customerId = customerId;
        order.number = number.trim();
        order.status = OrderStatus.AGUARDANDO_PAGAMENTO;
        order.subtotal = subtotal;
        order.discount = BigDecimal.ZERO;
        order.shippingFee = shippingFee;
        order.total = subtotal.add(shippingFee);
        order.createdAt = OffsetDateTime.now();
        order.updatedAt = order.createdAt;
        return order;
    }

    private static void requireAmount(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("Order amounts cannot be negative");
        }
    }

    public void changeStatus(OrderStatus target) {
        if (!OrderStatusTransitionPolicy.allows(status, target)) {
            throw new InvalidOrderStatusTransitionException(status, target);
        }
        status = target;
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getNumber() {
        return number;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
