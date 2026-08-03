package br.com.felipejorge.satireapi.domain.customer.internal.persistence;

import br.com.felipejorge.satireapi.domain.customer.internal.model.Profile;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByName(String name);
}
