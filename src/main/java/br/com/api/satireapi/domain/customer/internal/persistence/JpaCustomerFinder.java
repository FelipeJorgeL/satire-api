package br.com.api.satireapi.domain.customer.internal.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerFinder;

@Component
class JpaCustomerFinder implements CustomerFinder {

    private final CustomerRepository repository;

    JpaCustomerFinder(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repository.findById(id);
    }
}
