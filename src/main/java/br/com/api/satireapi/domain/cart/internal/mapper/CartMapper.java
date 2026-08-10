package br.com.api.satireapi.domain.cart.internal.mapper;

import br.com.api.satireapi.domain.cart.CartItemSnapshot;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchase;
import br.com.api.satireapi.domain.cart.internal.dto.response.CartItemResponse;
import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.model.Cart;
import br.com.api.satireapi.domain.cart.internal.model.CartItem;
import java.math.BigDecimal;
import java.util.List;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartItemResponse toResponse(
        CartItem item,
        ProductVariationPurchase variation
    ) {
        var subtotal = variation.unitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new CartItemResponse(
            item.getId(), variation.variationId(), variation.productId(), variation.productName(),
            variation.sku(), variation.variationName(), variation.unitPrice(), item.getQuantity(),
            subtotal, variation.stock(), variation.active() && variation.stock() >= item.getQuantity()
        );
    }

    public static CartResponse toResponse(Cart cart, List<CartItemResponse> items) {
        var subtotal = items.stream()
            .map(CartItemResponse::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getId(), cart.getCustomerId(), items, subtotal);
    }

    public static CartItemSnapshot toCheckoutSnapshot(
        CartItem item,
        ProductVariationPurchase variation
    ) {
        return new CartItemSnapshot(
            item.getId(), variation.variationId(), variation.productId(), variation.productName(),
            variation.sku(), variation.variationName(), variation.unitPrice(), variation.stock(),
            variation.active(), item.getQuantity()
        );
    }
}
