package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.internal.model.RefreshToken;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RefreshTokenStore;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    JpaRefreshTokenStore(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID customerId, String tokenHash, Instant expiresAt) {
        repository.upsert(customerId, tokenHash, expiresAt);
    }

    @Override
    public Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now) {
        return repository.findByTokenHashAndExpiresAtAfter(tokenHash, now)
            .map(RefreshToken::getCustomerId);
    }

    @Override
    public boolean rotateIfCurrent(
        UUID customerId,
        String currentTokenHash,
        Instant now,
        String nextTokenHash,
        Instant nextExpiresAt
    ) {
        return repository.rotateIfCurrent(
            customerId, currentTokenHash, now, nextTokenHash, nextExpiresAt
        ) == 1;
    }

    @Override
    public void deleteByCustomerId(UUID customerId) {
        repository.deleteById(customerId);
    }
}
