package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCustomerAddressRequest(
    @Size(max = 50) String label,
    @NotBlank @Size(max = 120) String recipient,
    @NotBlank @Pattern(regexp = "\\d{8}") String postalCode,
    @NotBlank @Size(max = 180) String street,
    @NotBlank @Size(max = 20) String number,
    @Size(max = 120) String complement,
    @NotBlank @Size(max = 100) String neighborhood,
    @NotBlank @Size(max = 100) String city,
    @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String state,
    Boolean primary
) {
}
