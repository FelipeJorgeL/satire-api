package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCustomerAccountRequest(
    @Size(min = 2, max = 120) @Pattern(regexp = ".*\\S.*") String name,
    @Size(max = 20) String phone
) {

    @AssertTrue(message = "Informe ao menos um campo para atualização")
    public boolean isAnyFieldPresent() {
        return name != null || phone != null;
    }
}
