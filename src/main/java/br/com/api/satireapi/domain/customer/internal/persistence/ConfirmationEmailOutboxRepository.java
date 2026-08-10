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
        update ConfirmationEmailOutbox outbox
           set outbox.status = :sending,
               outbox.attempts = outbox.attempts + 1,
               outbox.lastAttemptAt = :now,
               outbox.updatedAt = :now
         where outbox.id = :id
           and (
               outbox.status = :pending
               or (outbox.status = :failed and outbox.nextAttemptAt <= :now)
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
        select outbox.id
          from ConfirmationEmailOutbox outbox
         where outbox.status in (:pending, :failed)
           and outbox.nextAttemptAt <= :now
           and outbox.attempts < :maxAttempts
         order by outbox.createdAt
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
        update ConfirmationEmailOutbox outbox
           set outbox.status = :pending,
               outbox.nextAttemptAt = :now,
               outbox.updatedAt = :now
         where outbox.status = :sending
           and outbox.lastAttemptAt < :threshold
        """)
    int resetStale(
        @Param("threshold") Instant threshold,
        @Param("now") Instant now,
        @Param("pending") ConfirmationEmailOutbox.Status pending,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ConfirmationEmailOutbox outbox
           set outbox.status = :sent,
               outbox.updatedAt = :now,
               outbox.lastFailure = null
         where outbox.id = :id
           and outbox.status = :sending
        """)
    int markSent(
        @Param("id") UUID id,
        @Param("now") Instant now,
        @Param("sent") ConfirmationEmailOutbox.Status sent,
        @Param("sending") ConfirmationEmailOutbox.Status sending
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update ConfirmationEmailOutbox outbox
           set outbox.status = :failed,
               outbox.nextAttemptAt = :nextAttemptAt,
               outbox.lastFailure = :lastFailure,
               outbox.updatedAt = :now
         where outbox.id = :id
           and outbox.status = :sending
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
