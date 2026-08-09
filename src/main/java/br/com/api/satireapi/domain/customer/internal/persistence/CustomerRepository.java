package br.com.api.satireapi.domain.customer.internal.persistence;

import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.api.satireapi.domain.customer.internal.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    @EntityGraph(attributePaths = "profiles")
    @Query("select customer from Customer customer where customer.id = :id")
    Optional<Customer> findByIdWithProfiles(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select customer from Customer customer where customer.id = :id")
    Optional<Customer> findByIdForUpdate(@Param("id") UUID id);

    @Query("""
        select count(distinct customer.id)
        from Customer customer
        join customer.profiles profile
        where profile.name = :profileName
        """)
    long countByProfileName(@Param("profileName") String profileName);
}
