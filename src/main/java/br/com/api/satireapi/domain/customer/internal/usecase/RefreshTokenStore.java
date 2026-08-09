package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

    void save(UUID customerId, String tokenHash, Instant expiresAt);

    Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now);

    boolean rotateIfCurrent(
        UUID customerId,
        String currentTokenHash,
        Instant now,
        String nextTokenHash,
        Instant nextExpiresAt
    );

    void deleteByCustomerId(UUID customerId);
}
