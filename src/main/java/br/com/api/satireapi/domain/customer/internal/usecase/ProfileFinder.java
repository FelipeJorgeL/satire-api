package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.Optional;

import br.com.api.satireapi.domain.customer.internal.model.Profile;

public interface ProfileFinder {

    Optional<Profile> findByName(String name);
}
