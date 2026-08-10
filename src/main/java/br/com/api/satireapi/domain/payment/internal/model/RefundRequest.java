package br.com.api.satireapi.domain.payment.internal.model;

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
@Table(name = "estornos")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pagamento_id", nullable = false, unique = true)
    private UUID paymentId;

    @Column(name = "usuario_id", nullable = false)
    private UUID adminId;

    @Column(name = "chave_idempotencia", nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RefundStatus status;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "motivo", nullable = false, length = 255)
    private String reason;

    @Column(name = "transacao_gateway_id", unique = true, length = 255)
    private String gatewayTransactionId;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    protected RefundRequest() {
    }

    private RefundRequest(
        UUID paymentId,
        UUID adminId,
        String idempotencyKey,
        BigDecimal amount,
        String reason
    ) {
        this.paymentId = paymentId;
        this.adminId = adminId;
        this.idempotencyKey = idempotencyKey;
        this.status = RefundStatus.SOLICITADO;
        this.amount = amount;
        this.reason = reason;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static RefundRequest request(
        UUID paymentId,
        UUID adminId,
        String idempotencyKey,
        BigDecimal amount,
        String reason
    ) {
        if (paymentId == null || adminId == null || idempotencyKey == null
            || amount == null || amount.signum() <= 0 || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Dados da solicitação de estorno inválidos");
        }
        return new RefundRequest(
            paymentId, adminId, idempotencyKey.trim(), amount, reason.trim()
        );
    }

    public UUID getId() {
        return id;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public UUID getAdminId() {
        return adminId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
