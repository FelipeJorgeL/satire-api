package br.com.api.satireapi.infra.ratelimit;

import br.com.api.satireapi.domain.customer.RateLimitBucketStore;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import jakarta.persistence.EntityManager;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Repository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaRateLimitBucketStore implements RateLimitBucketStore {

    private final EntityManager entityManager;

    JpaRateLimitBucketStore(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean isBlocked(String key, int maxAttempts, Duration window, Instant now) {
        validate(key, maxAttempts, window);
        var cutoff = now.minus(window);
        var result = entityManager.createNativeQuery("""
            SELECT 1
            FROM rate_limit_buckets
            WHERE chave = :key
              AND janela_inicio > :cutoff
              AND tentativas >= :maxAttempts
            """)
            .setParameter("key", key)
            .setParameter("cutoff", cutoff)
            .setParameter("maxAttempts", maxAttempts)
            .getResultList();
        return !result.isEmpty();
    }

    @Override
    @Transactional
    public void recordFailure(String key, int maxAttempts, Duration window, Instant now) {
        validate(key, maxAttempts, window);
        entityManager.createNativeQuery("""
            INSERT INTO rate_limit_buckets (chave, janela_inicio, tentativas, atualizado_em)
            VALUES (:key, :now, 1, :now)
            ON CONFLICT (chave) DO UPDATE
            SET janela_inicio = CASE
                    WHEN rate_limit_buckets.janela_inicio <= :cutoff THEN :now
                    ELSE rate_limit_buckets.janela_inicio
                END,
                tentativas = CASE
                    WHEN rate_limit_buckets.janela_inicio <= :cutoff THEN 1
                    ELSE LEAST(rate_limit_buckets.tentativas + 1, :maxAttempts)
                END,
                atualizado_em = :now
            """)
            .setParameter("key", key)
            .setParameter("now", now)
            .setParameter("cutoff", now.minus(window))
            .setParameter("maxAttempts", maxAttempts)
            .executeUpdate();
    }

    @Override
    @Transactional
    public boolean tryAcquire(String key, int maxAttempts, Duration window, Instant now) {
        validate(key, maxAttempts, window);
        List<?> result = entityManager.createNativeQuery("""
            INSERT INTO rate_limit_buckets (chave, janela_inicio, tentativas, atualizado_em)
            VALUES (:key, :now, 1, :now)
            ON CONFLICT (chave) DO UPDATE
            SET janela_inicio = CASE
                    WHEN rate_limit_buckets.janela_inicio <= :cutoff THEN :now
                    ELSE rate_limit_buckets.janela_inicio
                END,
                tentativas = CASE
                    WHEN rate_limit_buckets.janela_inicio <= :cutoff THEN 1
                    ELSE rate_limit_buckets.tentativas + 1
                END,
                atualizado_em = :now
            WHERE rate_limit_buckets.janela_inicio <= :cutoff
               OR rate_limit_buckets.tentativas < :maxAttempts
            RETURNING chave
            """)
            .setParameter("key", key)
            .setParameter("now", now)
            .setParameter("cutoff", now.minus(window))
            .setParameter("maxAttempts", maxAttempts)
            .getResultList();
        return !result.isEmpty();
    }

    @Override
    @Transactional
    public void clear(String key) {
        entityManager.createNativeQuery("DELETE FROM rate_limit_buckets WHERE chave = :key")
            .setParameter("key", key)
            .executeUpdate();
    }

    @Scheduled(fixedDelayString = "${app.security.rate-limit-cleanup-interval:PT1H}")
    @Transactional
    public void removeStaleBuckets() {
        entityManager.createNativeQuery(
                "DELETE FROM rate_limit_buckets WHERE atualizado_em < :cutoff"
            )
            .setParameter("cutoff", Instant.now().minus(1, ChronoUnit.DAYS))
            .executeUpdate();
    }

    private static void validate(String key, int maxAttempts, Duration window) {
        if (key == null || key.isBlank() || key.length() > 80
            || maxAttempts < 1 || window.isNegative() || window.isZero()) {
            throw new IllegalArgumentException("Invalid rate limit bucket data");
        }
    }
}
