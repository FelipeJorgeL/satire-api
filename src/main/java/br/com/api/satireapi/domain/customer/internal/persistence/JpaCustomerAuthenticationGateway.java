package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaCustomerAuthenticationGateway implements CustomerAuthenticationGateway {

    private final CustomerRepository repository;

    JpaCustomerAuthenticationGateway(CustomerRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<CustomerAuthentication> findById(UUID customerId) {
        return repository.findByIdWithProfiles(customerId)
            .map(customer -> new CustomerAuthentication(
                customer.getId(),
                customer.isActive(),
                customer.getProfiles().stream()
                    .map(profile -> profile.getName())
                    .collect(java.util.stream.Collectors.toUnmodifiableSet())
            ));
    }
}
