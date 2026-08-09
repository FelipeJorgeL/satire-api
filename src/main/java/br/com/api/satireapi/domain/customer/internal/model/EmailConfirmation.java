package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "confirmacoes_email")
public class EmailConfirmation {

    @Id
    @Column(name = "usuario_id")
    private UUID customerId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumido_em")
    private Instant consumedAt;

    @Column(name = "criado_em", nullable = false)
    private Instant createdAt;

    protected EmailConfirmation() {
    }

    private EmailConfirmation(UUID customerId, String tokenHash, Instant expiresAt) {
        this.customerId = customerId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public static EmailConfirmation issue(UUID customerId, String tokenHash, Instant expiresAt) {
        return new EmailConfirmation(customerId, tokenHash, expiresAt);
    }

    public void rotate(String tokenHash, Instant expiresAt) {
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getConsumedAt() {
        return consumedAt;
    }
}
