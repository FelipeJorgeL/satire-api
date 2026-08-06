package br.com.api.satireapi.domain.customer.internal.persistence;

import java.util.Optional;
import org.springframework.stereotype.Component;

import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.domain.customer.internal.usecase.ProfileFinder;

@Component
class JpaProfileFinder implements ProfileFinder {

    private final ProfileRepository repository;

    JpaProfileFinder(ProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Profile> findByName(String name) {
        return repository.findByName(name);
    }
}
