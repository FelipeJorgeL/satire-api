package br.com.felipejorge.satireapi.domain.customer.internal.persistence;

import br.com.felipejorge.satireapi.domain.customer.CustomerGateway;
import br.com.felipejorge.satireapi.domain.customer.dto.CustomerSummary;
import br.com.felipejorge.satireapi.domain.customer.internal.model.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaCustomerGateway implements CustomerGateway {

    private final CustomerRepository repository;

    JpaCustomerGateway(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CustomerSummary> findById(UUID id) {
        return repository.findById(id).map(JpaCustomerGateway::toSummary);
    }

    @Override
    public Optional<CustomerSummary> findByEmail(String email) {
        return repository.findByEmail(Customer.normalizeEmail(email)).map(JpaCustomerGateway::toSummary);
    }

    private static CustomerSummary toSummary(Customer customer) {
        return new CustomerSummary(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.isActive()
        );
    }
}
