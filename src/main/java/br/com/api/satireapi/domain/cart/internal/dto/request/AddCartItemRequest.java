package br.com.api.satireapi.domain.cart.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record AddCartItemRequest(
    @NotNull UUID variationId,
    @NotNull @Positive @Max(99) Integer quantity
) {
}
