package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailConfirmationStore {

    void save(UUID customerId, String tokenHash, Instant expiresAt);

    Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now);

    boolean consume(UUID customerId, String tokenHash, Instant now);
}
