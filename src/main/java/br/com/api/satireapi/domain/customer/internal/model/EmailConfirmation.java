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

    public UUID getCustomerId() {
        return customerId;
    }
}
