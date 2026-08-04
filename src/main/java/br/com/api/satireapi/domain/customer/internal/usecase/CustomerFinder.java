package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.Optional;

import br.com.api.satireapi.domain.customer.internal.model.Customer;

public interface CustomerFinder {

    Optional<Customer> findByEmail(String email);
}
