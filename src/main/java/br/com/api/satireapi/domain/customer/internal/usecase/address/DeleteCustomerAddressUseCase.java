package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.persistence.AddressRepository;
import br.com.api.satireapi.domain.customer.internal.persistence.CustomerRepository;
import br.com.api.satireapi.domain.customer.internal.usecase.account.CustomerAccountNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCustomerAddressUseCase {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public DeleteCustomerAddressUseCase(
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
        var address = addresses.stream()
            .filter(candidate -> candidate.getId().equals(addressId))
            .findFirst()
            .orElseThrow(CustomerAddressNotFoundException::new);
        var wasPrimary = address.isPrimary();
        if (wasPrimary) {
            address.markPrimary(false);
        }
        addressRepository.delete(address);
        if (wasPrimary) {
            addresses.stream()
                .filter(candidate -> !candidate.getId().equals(addressId))
                .findFirst()
                .ifPresent(candidate -> candidate.markPrimary(true));
        }
    }

    private void requireActiveCustomer(UUID customerId) {
        customerRepository.findByIdForUpdate(customerId)
            .filter(customer -> customer.isActive())
            .orElseThrow(CustomerAccountNotFoundException::new);
    }
}
