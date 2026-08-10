package br.com.api.satireapi.domain.cart.internal.usecase.item;

import br.com.api.satireapi.domain.cart.internal.dto.request.AddCartItemRequest;
import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.model.Cart;
import br.com.api.satireapi.domain.cart.internal.model.CartItem;
import br.com.api.satireapi.domain.cart.internal.persistence.CartItemRepository;
import br.com.api.satireapi.domain.cart.internal.persistence.CartRepository;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemUnavailableException;
import br.com.api.satireapi.domain.cart.internal.usecase.CartQuantityExceededException;
import br.com.api.satireapi.domain.cart.internal.usecase.query.GetCartUseCase;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchaseGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddCartItemUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariationPurchaseGateway variationGateway;
    private final GetCartUseCase getCartUseCase;

    public AddCartItemUseCase(
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
    public CartResponse execute(UUID customerId, AddCartItemRequest request) {
        var variation = variationGateway.findById(request.variationId())
            .orElseThrow(CartItemUnavailableException::new);
        ensureAvailable(variation.active(), variation.stock(), request.quantity());
        var cart = cartRepository.findByCustomerIdForUpdate(customerId)
            .orElseGet(() -> cartRepository.save(Cart.forCustomer(customerId)));
        var item = itemRepository.findByCartIdAndVariationId(cart.getId(), request.variationId())
            .orElse(null);
        if (item == null) {
            itemRepository.save(CartItem.create(
                cart.getId(), request.variationId(), request.quantity()
            ));
        } else {
            var targetQuantity = Math.addExact(item.getQuantity(), request.quantity());
            if (targetQuantity > 99) {
                throw new CartQuantityExceededException();
            }
            ensureAvailable(variation.active(), variation.stock(), targetQuantity);
            item.changeQuantity(targetQuantity);
        }
        cart.touch();
        return getCartUseCase.execute(customerId);
    }

    private static void ensureAvailable(boolean active, int stock, int quantity) {
        if (!active || stock < quantity) {
            throw new CartItemUnavailableException();
        }
    }
}
