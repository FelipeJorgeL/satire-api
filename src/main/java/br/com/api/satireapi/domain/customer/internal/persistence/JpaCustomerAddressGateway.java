package br.com.api.satireapi.domain.customer.internal.persistence;

import br.com.api.satireapi.domain.customer.CustomerAddress;
import br.com.api.satireapi.domain.customer.CustomerAddressGateway;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaCustomerAddressGateway implements CustomerAddressGateway {

    private final AddressRepository addressRepository;

    JpaCustomerAddressGateway(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public Optional<CustomerAddress> findOwnedById(UUID customerId, UUID addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
            .map(address -> new CustomerAddress(
                address.getId(), address.getCustomerId(), address.getRecipient(),
                address.getPostalCode(), address.getStreet(), address.getNumber(),
                address.getComplement(), address.getNeighborhood(), address.getCity(),
                address.getState()
            ));
    }
}
