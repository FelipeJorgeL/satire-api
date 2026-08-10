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
        repository.upsert(customerId, tokenHash, expiresAt);
    }

    @Override
    public Optional<UUID> findCustomerIdByHash(String tokenHash, Instant now) {
        return repository.findByTokenHashAndExpiresAtAfter(tokenHash, now)
            .map(EmailConfirmation::getCustomerId);
    }

    @Override
    public boolean consume(UUID customerId, String tokenHash, Instant now) {
        return repository.consume(customerId, tokenHash, now) == 1;
    }

    @Override
    public void deleteByCustomerId(UUID customerId) {
        repository.deleteById(customerId);
    }
}
