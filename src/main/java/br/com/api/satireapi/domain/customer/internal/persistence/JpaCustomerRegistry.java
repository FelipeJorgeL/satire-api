package br.com.api.satireapi.domain.customer.internal.persistence;

import org.springframework.stereotype.Component;

import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerRegistry;

@Component
class JpaCustomerRegistry implements CustomerRegistry {

    private final CustomerRepository repository;

    JpaCustomerRegistry(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public Customer save(Customer customer) {
        return repository.save(customer);
    }
}
