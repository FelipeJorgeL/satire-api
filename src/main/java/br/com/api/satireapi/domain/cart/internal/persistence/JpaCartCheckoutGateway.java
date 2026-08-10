package br.com.api.satireapi.domain.cart.internal.persistence;

import br.com.api.satireapi.domain.cart.CartCheckoutGateway;
import br.com.api.satireapi.domain.cart.CartCheckoutSnapshot;
import br.com.api.satireapi.domain.cart.internal.mapper.CartMapper;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemUnavailableException;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchaseGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaCartCheckoutGateway implements CartCheckoutGateway {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariationPurchaseGateway variationGateway;

    JpaCartCheckoutGateway(
        CartRepository cartRepository,
        CartItemRepository itemRepository,
        ProductVariationPurchaseGateway variationGateway
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.variationGateway = variationGateway;
    }

    @Override
    @Transactional
    public CartCheckoutSnapshot loadForCheckout(UUID customerId) {
        var cart = cartRepository.findByCustomerIdForUpdate(customerId).orElse(null);
        if (cart == null) {
            return new CartCheckoutSnapshot(null, customerId, java.util.List.of());
        }
        var items = itemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId()).stream()
            .map(item -> CartMapper.toCheckoutSnapshot(
                item,
                variationGateway.findById(item.getVariationId())
                    .orElseThrow(CartItemUnavailableException::new)
            ))
            .toList();
        return new CartCheckoutSnapshot(cart.getId(), customerId, items);
    }

    @Override
    @Transactional
    public void clear(UUID customerId) {
        cartRepository.findByCustomerIdForUpdate(customerId).ifPresent(cart -> {
            itemRepository.deleteAllByCartId(cart.getId());
            cart.touch();
        });
    }
}
