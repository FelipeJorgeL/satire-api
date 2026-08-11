package br.com.api.satireapi.domain.payment.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_webhook_pagamentos")
public class PaymentWebhookEvent {

    @Id
    @Column(name = "evento_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "pagamento_id", nullable = false)
    private UUID paymentId;

    @Column(name = "hash_payload", nullable = false, length = 64)
    private String payloadHash;

    @Column(name = "recebido_em", nullable = false)
    private OffsetDateTime receivedAt;

    protected PaymentWebhookEvent() {
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getPayloadHash() {
        return payloadHash;
    }
}
