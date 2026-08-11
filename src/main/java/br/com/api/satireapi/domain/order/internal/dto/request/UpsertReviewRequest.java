package br.com.api.satireapi.domain.order.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertReviewRequest(
    @NotNull @Min(1) @Max(5) Short rating,
    @Size(max = 2_000) String comment
) {
}
