package br.com.api.satireapi.domain.cart.internal.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CartResponse(
    UUID id,
    UUID customerId,
    List<CartItemResponse> items,
    BigDecimal subtotal
) {

    public CartResponse {
        items = List.copyOf(items == null ? List.of() : items);
    }

    public static CartResponse empty(UUID customerId) {
        return new CartResponse(null, customerId, List.of(), BigDecimal.ZERO);
    }
}
