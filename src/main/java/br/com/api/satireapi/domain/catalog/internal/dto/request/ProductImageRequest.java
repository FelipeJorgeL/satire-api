package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProductImageRequest(
    @NotBlank @Pattern(regexp = "https://[^\\s]+") @Size(max = 2048) String url,
    @Size(max = 255) String altText,
    boolean decorative,
    boolean primary,
    @Min(0) int displayOrder
) {

    @AssertTrue(message = "Imagem decorativa não pode ter texto alternativo; imagem informativa exige altText")
    public boolean hasValidAlternativeText() {
        return decorative ? altText == null || altText.isBlank() : altText != null && !altText.isBlank();
    }
}
