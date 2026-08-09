package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.model.ConfirmationEmailOutbox;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConfirmationEmailOutboxRepository extends JpaRepository<ConfirmationEmailOutbox, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ConfirmationEmailOutbox o
           SET o.status = :sending,
               o.attempts = o.attempts + 1,
               o.lastAttemptAt = :now,
               o.updatedAt = :now
         WHERE o.id = :id
           AND (
               o.status = :pending
               OR (o.status = :failed AND o.nextAttemptAt <= :now)
           )
        """)
    int claim(
        @Param("id") UUID id,
        @Param("now") Instant now,
        @Param("pending") ConfirmationEmailOutbox.Status pending,
        @Param("failed") ConfirmationEmailOutbox.Status failed,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );

    @Query("""
        SELECT o.id
          FROM ConfirmationEmailOutbox o
         WHERE o.status IN (:pending, :failed)
           AND o.nextAttemptAt <= :now
           AND o.attempts < :maxAttempts
         ORDER BY o.createdAt
        """)
    List<UUID> findReady(
        @Param("now") Instant now,
        @Param("maxAttempts") int maxAttempts,
        @Param("pending") ConfirmationEmailOutbox.Status pending,
        @Param("failed") ConfirmationEmailOutbox.Status failed,
        Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ConfirmationEmailOutbox o
           SET o.status = :pending,
               o.nextAttemptAt = :now,
               o.updatedAt = :now
         WHERE o.status = :sending
           AND o.lastAttemptAt < :threshold
        """)
    int resetStale(
        @Param("threshold") Instant threshold,
        @Param("now") Instant now,
        @Param("pending") ConfirmationEmailOutbox.Status pending,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ConfirmationEmailOutbox o
           SET o.status = :sent,
               o.updatedAt = :now,
               o.lastFailure = NULL
         WHERE o.id = :id
           AND o.status = :sending
        """)
    int markSent(
        @Param("id") UUID id,
        @Param("now") Instant now,
        @Param("sent") ConfirmationEmailOutbox.Status sent,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE ConfirmationEmailOutbox o
           SET o.status = :failed,
               o.nextAttemptAt = :nextAttemptAt,
               o.lastFailure = :lastFailure,
               o.updatedAt = :now
         WHERE o.id = :id
           AND o.status = :sending
        """)
    int markFailed(
        @Param("id") UUID id,
        @Param("nextAttemptAt") Instant nextAttemptAt,
        @Param("lastFailure") String lastFailure,
        @Param("now") Instant now,
        @Param("failed") ConfirmationEmailOutbox.Status failed,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );
}
