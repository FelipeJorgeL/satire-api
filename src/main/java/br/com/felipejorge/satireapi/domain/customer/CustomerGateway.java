package br.com.felipejorge.satireapi.domain.customer;

import br.com.felipejorge.satireapi.domain.customer.dto.CustomerSummary;
import java.util.Optional;
import java.util.UUID;

public interface CustomerGateway {

    Optional<CustomerSummary> findById(UUID id);

    Optional<CustomerSummary> findByEmail(String email);
}
