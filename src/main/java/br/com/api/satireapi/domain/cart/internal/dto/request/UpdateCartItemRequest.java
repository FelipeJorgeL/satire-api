package br.com.api.satireapi.domain.cart.internal.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateCartItemRequest(
    @NotNull @Positive @Max(99) Integer quantity
) {
}
