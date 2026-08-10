package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 120) String slug
) {
}
