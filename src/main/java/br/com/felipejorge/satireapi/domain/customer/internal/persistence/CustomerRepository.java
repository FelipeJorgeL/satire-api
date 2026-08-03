package br.com.felipejorge.satireapi.domain.customer.internal.persistence;

import br.com.felipejorge.satireapi.domain.customer.internal.model.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);
}
