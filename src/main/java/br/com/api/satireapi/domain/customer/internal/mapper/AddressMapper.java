package br.com.api.satireapi.domain.customer.internal.mapper;

import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.model.Address;

public final class AddressMapper {

    private AddressMapper() {
    }

    public static CustomerAddressResponse toResponse(Address address) {
        return new CustomerAddressResponse(
            address.getId(),
            address.getLabel(),
            address.getRecipient(),
            address.getPostalCode(),
            address.getStreet(),
            address.getNumber(),
            address.getComplement(),
            address.getNeighborhood(),
            address.getCity(),
            address.getState(),
            address.isPrimary(),
            address.getCreatedAt()
        );
    }
}
