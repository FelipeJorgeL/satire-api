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

    Optional<Order> findByIdAndCustomerId(UUID id, UUID customerId);

    boolean existsByIdAndCustomerId(UUID id, UUID customerId);

    @Query(value = """
        SELECT order_entity.id
        FROM pedidos order_entity
        JOIN itens_pedidos item ON item.pedido_id = order_entity.id
        WHERE order_entity.usuario_id = :customerId
          AND order_entity.status = 'ENTREGUE'
          AND item.produto_id = :productId
        ORDER BY order_entity.atualizado_em DESC, order_entity.id
        LIMIT 1
        """, nativeQuery = true)
    Optional<UUID> findDeliveredPurchaseOrderId(
        @Param("customerId") UUID customerId,
        @Param("productId") UUID productId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select orderEntity from Order orderEntity
        where orderEntity.id = :id and orderEntity.customerId = :customerId
        """)
    Optional<Order> findByIdAndCustomerIdForUpdate(
        @Param("id") UUID id,
        @Param("customerId") UUID customerId
    );
}
