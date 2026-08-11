package br.com.api.satireapi.domain.order.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelCustomerOrderRequest(
    @NotBlank @Size(max = 255) String reason
) {
}
