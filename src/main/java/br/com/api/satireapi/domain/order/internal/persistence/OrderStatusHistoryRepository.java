package br.com.api.satireapi.domain.order.internal.persistence;

import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findAllByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
