package br.com.api.satireapi.domain.cart.internal.usecase.item;

import br.com.api.satireapi.domain.cart.internal.persistence.CartItemRepository;
import br.com.api.satireapi.domain.cart.internal.persistence.CartRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClearCartUseCase {

    private final CartRepository cartRepository;
    private final CartItemRepository itemRepository;

    public ClearCartUseCase(
        CartRepository cartRepository,
        CartItemRepository itemRepository
    ) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
    }

    @Transactional
    public void execute(UUID customerId) {
        cartRepository.findByCustomerIdForUpdate(customerId).ifPresent(cart -> {
            itemRepository.deleteAllByCartId(cart.getId());
            cart.touch();
        });
    }
}
