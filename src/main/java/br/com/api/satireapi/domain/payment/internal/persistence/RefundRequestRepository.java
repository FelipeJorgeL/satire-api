package br.com.api.satireapi.domain.payment.internal.persistence;

import br.com.api.satireapi.domain.payment.internal.model.RefundRequest;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, UUID> {

    Optional<RefundRequest> findByIdempotencyKey(String idempotencyKey);

    boolean existsByPaymentId(UUID paymentId);
}
