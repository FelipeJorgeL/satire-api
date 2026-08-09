package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationStore {

    void save(UUID customerId, String tokenHash, Instant expiresAt);

    Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now);

    void deleteByCustomerId(UUID customerId);
}
