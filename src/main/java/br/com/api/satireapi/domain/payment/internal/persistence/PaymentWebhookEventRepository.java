package br.com.api.satireapi.domain.payment.internal.persistence;

import br.com.api.satireapi.domain.payment.internal.model.PaymentWebhookEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentWebhookEventRepository
    extends JpaRepository<PaymentWebhookEvent, String> {

    @Modifying
    @Query(value = """
        INSERT INTO eventos_webhook_pagamentos
            (evento_id, pagamento_id, hash_payload, recebido_em)
        VALUES (:eventId, :paymentId, :payloadHash, CURRENT_TIMESTAMP)
        ON CONFLICT (evento_id) DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
        @Param("eventId") String eventId,
        @Param("paymentId") UUID paymentId,
        @Param("payloadHash") String payloadHash
    );
}
