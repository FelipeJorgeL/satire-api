package br.com.felipejorge.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterCustomerRequest(
    @NotBlank @Size(min = 2, max = 120) String name,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(min = 8, max = 72) String password,
    @Pattern(regexp = "^\\d{11}$") String cpf,
    @Size(max = 20) String phone
) {
}
