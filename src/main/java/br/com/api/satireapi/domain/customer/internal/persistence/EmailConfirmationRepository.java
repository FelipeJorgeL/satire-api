package br.com.api.satireapi.domain.customer.internal.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.api.satireapi.domain.customer.internal.model.EmailConfirmation;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, UUID> {

    Optional<EmailConfirmation> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);
}
