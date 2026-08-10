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
@Table(name = "carrinhos")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID customerId;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected Cart() {
    }

    private Cart(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        this.customerId = customerId;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Cart forCustomer(UUID customerId) {
        return new Cart(customerId);
    }

    public void touch() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
