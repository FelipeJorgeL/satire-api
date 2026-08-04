package br.com.api.satireapi.domain.customer.internal.usecase;

import br.com.api.satireapi.domain.customer.internal.model.Customer;

public interface CustomerRegistry {

    boolean existsByEmail(String email);

    Customer save(Customer customer);
}
