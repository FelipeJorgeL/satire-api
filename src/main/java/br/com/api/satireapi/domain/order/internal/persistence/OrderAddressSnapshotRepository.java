package br.com.api.satireapi.domain.order.internal.persistence;

import br.com.api.satireapi.domain.order.internal.model.OrderAddressSnapshot;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderAddressSnapshotRepository extends JpaRepository<OrderAddressSnapshot, UUID> {

    Optional<OrderAddressSnapshot> findByOrderId(UUID orderId);
}
