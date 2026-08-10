package br.com.api.satireapi.domain.cart.internal.persistence;

import br.com.api.satireapi.domain.cart.internal.model.Cart;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByCustomerId(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cart from Cart cart where cart.customerId = :customerId")
    Optional<Cart> findByCustomerIdForUpdate(@Param("customerId") UUID customerId);
}
