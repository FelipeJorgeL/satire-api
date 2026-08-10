package br.com.api.satireapi.domain.payment.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RequestPaymentRefund(
    @NotBlank @Size(min = 3, max = 255) String reason
) {
}
