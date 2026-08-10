package br.com.api.satireapi.domain.cart.internal.usecase.item;

import br.com.api.satireapi.domain.cart.internal.persistence.CartItemRepository;
import br.com.api.satireapi.domain.cart.internal.persistence.CartRepository;
import br.com.api.satireapi.domain.cart.internal.usecase.CartItemNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;

    public RemoveCartItemUseCase(
        CartRepository cartRepository,
        CartItemRepository itemRepository
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void execute(UUID customerId, UUID itemId) {
        var cart = cartRepository.findByCustomerIdForUpdate(customerId)
            .orElseThrow(CartItemNotFoundException::new);
        var item = itemRepository.findByIdAndCartIdForUpdate(itemId, cart.getId())
            .orElseThrow(CartItemNotFoundException::new);
        itemRepository.delete(item);
        cart.touch();
    }
}
