package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.model.RefreshToken;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Query("""
        select token
          from RefreshToken token
         where token.tokenHash = :tokenHash
           and token.expiresAt > :now
        """)
    Optional<RefreshToken> findByTokenHashAndExpiresAtAfter(
        @Param("tokenHash") String tokenHash,
        @Param("now") Instant now
    );

    @Modifying
    @Query(value = """
        INSERT INTO refresh_tokens (usuario_id, token_hash, expira_em)
        VALUES (:customerId, :tokenHash, :expiresAt)
        ON CONFLICT (usuario_id) DO UPDATE
            SET token_hash = EXCLUDED.token_hash,
                expira_em = EXCLUDED.expira_em
        """, nativeQuery = true)
    int upsert(
        @Param("customerId") UUID customerId,
        @Param("tokenHash") String tokenHash,
        @Param("expiresAt") Instant expiresAt
    );

    @Modifying
    @Query(value = """
        UPDATE refresh_tokens
           SET token_hash = :nextTokenHash,
               expira_em = :nextExpiresAt
         WHERE usuario_id = :customerId
           AND token_hash = :currentTokenHash
           AND expira_em > :now
        """, nativeQuery = true)
    int rotateIfCurrent(
        @Param("customerId") UUID customerId,
        @Param("currentTokenHash") String currentTokenHash,
        @Param("now") Instant now,
        @Param("nextTokenHash") String nextTokenHash,
        @Param("nextExpiresAt") Instant nextExpiresAt
    );
}
