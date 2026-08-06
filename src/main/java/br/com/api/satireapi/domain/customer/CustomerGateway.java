package br.com.api.satireapi.domain.customer;

import java.util.Optional;
import java.util.UUID;

import br.com.api.satireapi.domain.customer.dto.CustomerSummary;

public interface CustomerGateway {

    Optional<CustomerSummary> findById(UUID id);

    Optional<CustomerSummary> findByEmail(String email);
}
