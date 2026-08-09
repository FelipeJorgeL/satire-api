package br.com.api.satireapi.domain.customer.internal.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.api.satireapi.domain.customer.internal.model.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select profile from Profile profile where profile.name = :name")
    Optional<Profile> findByNameForUpdate(@Param("name") String name);
}
