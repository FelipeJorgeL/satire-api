package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductVariationRequest(
    @NotBlank @Size(min = 3, max = 60) String sku,
    @NotBlank @Size(max = 120) String name,
    @NotNull @DecimalMin(value = "0.00") BigDecimal price,
    @Min(0) int stock
) {
}
