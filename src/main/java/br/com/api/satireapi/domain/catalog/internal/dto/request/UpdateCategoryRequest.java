package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
    @Size(min = 2, max = 100) String name,
    @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 120) String slug
) {

    @AssertTrue(message = "Informe ao menos um campo para atualização")
    public boolean isAnyFieldPresent() {
        return name != null || slug != null;
    }
}
