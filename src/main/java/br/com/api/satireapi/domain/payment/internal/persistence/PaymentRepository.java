package br.com.api.satireapi.domain.payment.internal.persistence;

import br.com.api.satireapi.domain.payment.internal.model.Payment;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findAllByOrderIdOrderByCreatedAtDesc(UUID orderId);

    boolean existsByGatewayTransactionIdAndIdNot(String gatewayTransactionId, UUID id);

    @Modifying
    @Query(value = """
        INSERT INTO pagamentos
            (id, pedido_id, metodo, status, valor, chave_idempotencia, criado_em, atualizado_em)
        VALUES
            (:paymentId, :orderId, :method, 'PENDENTE', :amount, :idempotencyKey,
             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (chave_idempotencia) DO NOTHING
        """, nativeQuery = true)
    int insertPendingIfAbsent(
        @Param("paymentId") UUID paymentId,
        @Param("orderId") UUID orderId,
        @Param("method") String method,
        @Param("amount") BigDecimal amount,
        @Param("idempotencyKey") String idempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select payment from Payment payment where payment.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") UUID id);
}
