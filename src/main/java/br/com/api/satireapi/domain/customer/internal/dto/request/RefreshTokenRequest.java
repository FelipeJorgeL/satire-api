package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshTokenRequest(
    @NotBlank @Size(max = 256) String refreshToken
) {
}
