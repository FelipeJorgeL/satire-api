package br.com.api.satireapi.domain.customer.internal.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import br.com.api.satireapi.domain.customer.internal.model.RefreshToken;
import br.com.api.satireapi.domain.customer.internal.usecase.RefreshTokenStore;

@Component
class JpaRefreshTokenStore implements RefreshTokenStore {

    private final RefreshTokenRepository repository;

    JpaRefreshTokenStore(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID customerId, String tokenHash, Instant expiresAt) {
        var token = repository.findById(customerId)
            .map(existing -> {
                existing.rotate(tokenHash, expiresAt);
                return existing;
            })
            .orElseGet(() -> RefreshToken.issue(customerId, tokenHash, expiresAt));
        repository.save(token);
    }

    @Override
    public Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now) {
        return repository.findByTokenHashAndExpiresAtAfter(tokenHash, now).map(RefreshToken::getCustomerId);
    }

    @Override
    public void deleteByCustomerId(UUID customerId) {
        repository.deleteById(customerId);
    }
}
