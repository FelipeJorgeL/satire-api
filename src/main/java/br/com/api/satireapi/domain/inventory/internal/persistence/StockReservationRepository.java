package br.com.api.satireapi.domain.inventory.internal.persistence;

import br.com.api.satireapi.domain.inventory.internal.model.StockReservation;
import br.com.api.satireapi.domain.inventory.internal.model.StockReservationStatus;
import jakarta.persistence.LockModeType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select reservation from StockReservation reservation where reservation.orderId = :orderId")
    Optional<StockReservation> findByOrderIdForUpdate(@Param("orderId") UUID orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select reservation
        from StockReservation reservation
        where reservation.status = :status
          and reservation.expiresAt <= :now
        order by reservation.expiresAt asc
        """)
    List<StockReservation> findExpiredForUpdate(
        @Param("status") StockReservationStatus status,
        @Param("now") OffsetDateTime now
    );
}
