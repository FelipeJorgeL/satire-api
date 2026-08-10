package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerAddressRequest(
    @Size(max = 50) String label,
    @Size(max = 120) String recipient,
    @Pattern(regexp = "\\d{8}") String postalCode,
    @Size(max = 180) String street,
    @Size(max = 20) String number,
    @Size(max = 120) String complement,
    @Size(max = 100) String neighborhood,
    @Size(max = 100) String city,
    @Pattern(regexp = "[A-Za-z]{2}") String state
) {

    @AssertTrue(message = "Informe ao menos um campo para atualização")
    public boolean isAnyFieldPresent() {
        return label != null || recipient != null || postalCode != null || street != null
            || number != null || complement != null || neighborhood != null || city != null || state != null;
    }
}
