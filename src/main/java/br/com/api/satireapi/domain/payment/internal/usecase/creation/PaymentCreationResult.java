package br.com.api.satireapi.domain.payment.internal.usecase.creation;

import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;

public record PaymentCreationResult(PaymentResponse payment, boolean created) {
}
