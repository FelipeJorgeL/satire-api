package br.com.api.satireapi.domain.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerAuthenticationGateway {

    Optional<CustomerAuthentication> findById(UUID customerId);
}
