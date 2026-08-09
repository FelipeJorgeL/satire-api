package br.com.api.satireapi.domain.customer.internal.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.api.satireapi.domain.customer.internal.model.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);
}
