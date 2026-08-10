package br.com.api.satireapi.domain.cart.internal.usecase.item;

import br.com.api.satireapi.domain.cart.internal.dto.request.UpdateCartItemRequest;
import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.persistence.CartItemRepository;
import br.com.api.satireapi.domain.cart.internal.persistence.CartRepository;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemNotFoundException;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemUnavailableException;
import br.com.api.satireapi.domain.cart.internal.usecase.query.GetCartUseCase;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchaseGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCartItemUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariationPurchaseGateway variationGateway;
    private final GetCartUseCase getCartUseCase;

    public UpdateCartItemUseCase(
        CartRepository cartRepository,
        CartItemRepository itemRepository,
        ProductVariationPurchaseGateway variationGateway,
        GetCartUseCase getCartUseCase
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.variationGateway = variationGateway;
        this.getCartUseCase = getCartUseCase;
    }

    @Transactional
    public CartResponse execute(
        UUID customerId,
        UUID itemId,
        UpdateCartItemRequest request
    ) {
        var cart = cartRepository.findByCustomerIdForUpdate(customerId)
            .orElseThrow(CartItemNotFoundException::new);
        var item = itemRepository.findByIdAndCartIdForUpdate(itemId, cart.getId())
            .orElseThrow(CartItemNotFoundException::new);
        var variation = variationGateway.findById(item.getVariationId())
            .orElseThrow(CartItemUnavailableException::new);
        if (!variation.active() || variation.stock() < request.quantity()) {
            throw new CartItemUnavailableException();
        }
        item.changeQuantity(request.quantity());
        cart.touch();
        return getCartUseCase.execute(customerId);
    }
}
