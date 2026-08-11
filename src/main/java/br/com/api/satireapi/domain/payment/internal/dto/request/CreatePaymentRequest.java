package br.com.api.satireapi.domain.payment.internal.dto.request;

import br.com.api.satireapi.domain.payment.internal.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(@NotNull PaymentMethod method) {
}
