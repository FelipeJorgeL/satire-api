package br.com.api.satireapi.domain.order.internal.persistence;

import br.com.api.satireapi.domain.order.internal.model.Order;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID>, JpaSpecificationExecutor<Order> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orderEntity from Order orderEntity where orderEntity.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") UUID id);
}
