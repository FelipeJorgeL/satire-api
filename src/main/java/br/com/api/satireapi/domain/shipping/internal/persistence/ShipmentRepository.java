package br.com.api.satireapi.domain.shipping.internal.persistence;

import br.com.api.satireapi.domain.shipping.internal.model.Shipment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    Optional<Shipment> findByOrderId(UUID orderId);

    boolean existsByTrackingCodeAndIdNot(String trackingCode, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shipment from Shipment shipment where shipment.orderId = :orderId")
    Optional<Shipment> findByOrderIdForUpdate(@Param("orderId") UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select shipment from Shipment shipment where shipment.id = :id")
    Optional<Shipment> findByIdForUpdate(@Param("id") UUID id);
}
