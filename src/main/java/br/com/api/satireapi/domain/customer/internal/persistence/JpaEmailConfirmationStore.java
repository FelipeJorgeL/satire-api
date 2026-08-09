package br.com.api.satireapi.domain.customer.internal.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import br.com.api.satireapi.domain.customer.internal.model.EmailConfirmation;
import br.com.api.satireapi.domain.customer.internal.usecase.EmailConfirmationStore;

@Component
class JpaEmailConfirmationStore implements EmailConfirmationStore {

    private final EmailConfirmationRepository repository;

    JpaEmailConfirmationStore(EmailConfirmationRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(UUID customerId, String tokenHash, Instant expiresAt) {
        var confirmation = repository.findById(customerId)
            .map(existing -> {
                existing.rotate(tokenHash, expiresAt);
                return existing;
            })
            .orElseGet(() -> EmailConfirmation.issue(customerId, tokenHash, expiresAt));
        repository.save(confirmation);
    }

    @Override
    public Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now) {
        return repository.findByTokenHashAndExpiresAtAfter(tokenHash, now).map(EmailConfirmation::getCustomerId);
    }

    @Override
    public void deleteByCustomerId(UUID customerId) {
        repository.deleteById(customerId);
    }
}
