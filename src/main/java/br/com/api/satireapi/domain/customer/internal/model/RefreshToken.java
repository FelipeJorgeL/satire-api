package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(name = "usuario_id")
    private UUID customerId;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private Instant expiresAt;

    @Column(name = "criado_em", nullable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    private RefreshToken(UUID customerId, String tokenHash, Instant expiresAt) {
        this.customerId = customerId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public static RefreshToken issue(UUID customerId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(customerId, tokenHash, expiresAt);
    }

    public UUID getCustomerId() {
        return customerId;
    }
}
