package br.com.api.satireapi.domain.customer.internal.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.api.satireapi.domain.customer.internal.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByName(String name);
}
