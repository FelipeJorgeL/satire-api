package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAdminCustomerRequest(
    @Size(min = 2, max = 120) @Pattern(regexp = ".*\\S.*") String name,
    @Email @Size(min = 3, max = 255) String email,
    @Pattern(regexp = "^$|^\\d{11}$") String cpf,
    @Size(max = 20) String phone
) {

    @AssertTrue(message = "Informe ao menos um campo para atualização")
    public boolean isAnyFieldPresent() {
        return name != null || email != null || cpf != null || phone != null;
    }
}
