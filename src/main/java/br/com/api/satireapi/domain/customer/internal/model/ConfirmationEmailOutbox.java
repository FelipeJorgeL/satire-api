package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "confirmacoes_email_outbox")
public class ConfirmationEmailOutbox {

    public enum Status {
        PENDING,
        SENDING,
        SENT,
        FAILED
    }

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID customerId;

    @Column(name = "destinatario", nullable = false, length = 255)
    private String recipient;

    @Column(name = "link_cifrado", nullable = false)
    private String protectedLink;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "tentativas", nullable = false)
    private int attempts;

    @Column(name = "proxima_tentativa_em", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "ultima_tentativa_em")
    private Instant lastAttemptAt;

    @Column(name = "ultima_falha", length = 500)
    private String lastFailure;

    @Column(name = "criado_em", nullable = false)
    private Instant createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private Instant updatedAt;

    protected ConfirmationEmailOutbox() {
    }

    private ConfirmationEmailOutbox(UUID customerId, String recipient, String protectedLink, Instant now) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.recipient = recipient;
        this.protectedLink = protectedLink;
        this.status = Status.PENDING;
        this.nextAttemptAt = now;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static ConfirmationEmailOutbox queue(
        UUID customerId,
        String recipient,
        String protectedLink,
        Instant now
    ) {
        return new ConfirmationEmailOutbox(customerId, recipient, protectedLink, now);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getProtectedLink() {
        return protectedLink;
    }

    public int getAttempts() {
        return attempts;
    }
}
