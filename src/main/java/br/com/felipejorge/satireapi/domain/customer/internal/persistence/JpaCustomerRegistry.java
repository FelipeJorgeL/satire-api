package br.com.felipejorge.satireapi.domain.customer.internal.persistence;

import br.com.felipejorge.satireapi.domain.customer.internal.model.Customer;
import br.com.felipejorge.satireapi.domain.customer.internal.usecase.CustomerRegistry;
import org.springframework.stereotype.Component;

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
