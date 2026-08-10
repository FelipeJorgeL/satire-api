package br.com.api.satireapi.domain.customer;

import java.util.Optional;
import java.util.UUID;

public interface CustomerAddressGateway {

    Optional<CustomerAddress> findOwnedById(UUID customerId, UUID addressId);
}
