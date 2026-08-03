package br.com.felipejorge.satireapi.domain.customer.internal.usecase;

import br.com.felipejorge.satireapi.domain.customer.internal.model.Profile;
import java.util.Optional;

public interface ProfileFinder {

    Optional<Profile> findByName(String name);
}
