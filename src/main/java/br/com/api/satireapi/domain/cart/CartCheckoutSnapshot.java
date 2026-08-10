package br.com.api.satireapi.domain.cart;

import java.util.List;
import java.util.UUID;

public record CartCheckoutSnapshot(
    UUID cartId,
    UUID customerId,
    List<CartItemSnapshot> items
) {

    public CartCheckoutSnapshot {
        items = List.copyOf(items == null ? List.of() : items);
    }
}
