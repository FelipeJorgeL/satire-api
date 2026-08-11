package br.com.api.satireapi.domain.payment.internal.dto.request;

import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;

public record PaymentWebhookRequest(
    PaymentStatus status,
    String gatewayTransactionId
) {
}
