package br.com.api.satireapi.domain.cart.internal.persistence;

import br.com.api.satireapi.domain.cart.internal.model.CartItem;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findAllByCartIdOrderByCreatedAtAsc(UUID cartId);

    Optional<CartItem> findByCartIdAndVariationId(UUID cartId, UUID variationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select item from CartItem item where item.id = :itemId and item.cartId = :cartId")
    Optional<CartItem> findByIdAndCartIdForUpdate(
        @Param("itemId") UUID itemId,
        @Param("cartId") UUID cartId
    );

    void deleteAllByCartId(UUID cartId);
}
