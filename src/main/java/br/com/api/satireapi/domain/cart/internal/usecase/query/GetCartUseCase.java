package br.com.api.satireapi.domain.cart.internal.usecase.query;

import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.mapper.CartMapper;
import br.com.api.satireapi.domain.cart.internal.persistence.CartItemRepository;
import br.com.api.satireapi.domain.cart.internal.persistence.CartRepository;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemUnavailableException;
import br.com.api.satireapi.domain.catalog.ProductVariationPurchaseGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final ProductVariationPurchaseGateway variationGateway;

    public GetCartUseCase(
        CartRepository cartRepository,
        CartItemRepository itemRepository,
        ProductVariationPurchaseGateway variationGateway
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.variationGateway = variationGateway;
    }

    @Transactional(readOnly = true)
    public CartResponse execute(UUID customerId) {
        var cart = cartRepository.findByCustomerId(customerId)
            .orElse(null);
        if (cart == null) {
            return CartResponse.empty(customerId);
        }
        var items = itemRepository.findAllByCartIdOrderByCreatedAtAsc(cart.getId()).stream()
            .map(item -> CartMapper.toResponse(item, variation(item.getVariationId())))
            .toList();
        return CartMapper.toResponse(cart, items);
    }

    private br.com.api.satireapi.domain.catalog.ProductVariationPurchase variation(UUID id) {
        return variationGateway.findById(id).orElseThrow(CartItemUnavailableException::new);
    }
}
