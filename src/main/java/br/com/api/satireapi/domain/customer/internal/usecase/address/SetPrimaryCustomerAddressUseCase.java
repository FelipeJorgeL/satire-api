package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.persistence.AddressRepository;
import br.com.api.satireapi.domain.customer.internal.persistence.CustomerRepository;
import br.com.api.satireapi.domain.customer.internal.usecase.account.CustomerAccountNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetPrimaryCustomerAddressUseCase {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public SetPrimaryCustomerAddressUseCase(
        CustomerRepository customerRepository,
        AddressRepository addressRepository
    ) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public void execute(UUID customerId, UUID addressId) {
        requireActiveCustomer(customerId);
        var addresses = addressRepository.findAllByCustomerIdForUpdate(customerId);
        var targetExists = addresses.stream().anyMatch(address -> address.getId().equals(addressId));
        if (!targetExists) {
            throw new CustomerAddressNotFoundException();
        }
        addresses.forEach(address -> address.markPrimary(address.getId().equals(addressId)));
    }

    private void requireActiveCustomer(UUID customerId) {
        customerRepository.findByIdForUpdate(customerId)
            .filter(customer -> customer.isActive())
            .orElseThrow(CustomerAccountNotFoundException::new);
    }
}
