package br.com.api.satireapi.domain.customer.internal.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.api.satireapi.domain.customer.internal.model.EmailConfirmation;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, UUID> {

    @Query("""
        SELECT e
          FROM EmailConfirmation e
         WHERE e.tokenHash = :tokenHash
           AND e.expiresAt > :now
           AND e.consumedAt IS NULL
        """)
    Optional<EmailConfirmation> findByTokenHashAndExpiresAtAfter(
        @Param("tokenHash") String tokenHash,
        @Param("now") Instant now
    );

    @Modifying
    @Query(value = """
        INSERT INTO confirmacoes_email (usuario_id, token_hash, expira_em)
        VALUES (:customerId, :tokenHash, :expiresAt)
        ON CONFLICT (usuario_id) DO UPDATE
            SET token_hash = EXCLUDED.token_hash,
                expira_em = EXCLUDED.expira_em,
                consumido_em = NULL
        """, nativeQuery = true)
    int upsert(
        @Param("customerId") UUID customerId,
        @Param("tokenHash") String tokenHash,
        @Param("expiresAt") Instant expiresAt
    );

    @Modifying
    @Query(value = """
        UPDATE confirmacoes_email
           SET consumido_em = :now
         WHERE usuario_id = :customerId
           AND token_hash = :tokenHash
           AND expira_em > :now
           AND consumido_em IS NULL
        """, nativeQuery = true)
    int consume(
        @Param("customerId") UUID customerId,
        @Param("tokenHash") String tokenHash,
        @Param("now") Instant now
    );
}
