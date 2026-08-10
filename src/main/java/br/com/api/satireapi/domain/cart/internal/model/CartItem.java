package br.com.api.satireapi.domain.cart.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "itens_carrinhos")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "carrinho_id", nullable = false)
    private UUID cartId;

    @Column(name = "variacao_produto_id", nullable = false)
    private UUID variationId;

    @Column(name = "quantidade", nullable = false)
    private int quantity;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected CartItem() {
    }

    private CartItem(UUID cartId, UUID variationId, int quantity) {
        validateQuantity(quantity);
        this.cartId = cartId;
        this.variationId = variationId;
        this.quantity = quantity;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static CartItem create(UUID cartId, UUID variationId, int quantity) {
        if (cartId == null || variationId == null) {
            throw new IllegalArgumentException("Cart and variation are required");
        }
        return new CartItem(cartId, variationId, quantity);
    }

    public void changeQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
        this.updatedAt = OffsetDateTime.now();
    }

    private static void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > 99) {
            throw new IllegalArgumentException("Cart quantity must be between 1 and 99");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCartId() {
        return cartId;
    }

    public UUID getVariationId() {
        return variationId;
    }

    public int getQuantity() {
        return quantity;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
