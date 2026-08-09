package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResendConfirmationRequest(
    @NotBlank @Email @Size(max = 255) String email
) {
}
