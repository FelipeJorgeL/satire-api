package br.com.felipejorge.satireapi.domain.customer.internal.persistence;

import br.com.felipejorge.satireapi.domain.customer.internal.model.Profile;
import br.com.felipejorge.satireapi.domain.customer.internal.usecase.ProfileFinder;
import java.util.Optional;
import org.springframework.stereotype.Component;

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
