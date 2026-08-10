package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.dto.request.CreateCustomerAddressRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.mapper.AddressMapper;
import br.com.api.satireapi.domain.customer.internal.model.Address;
import br.com.api.satireapi.domain.customer.internal.persistence.AddressRepository;
import br.com.api.satireapi.domain.customer.internal.persistence.CustomerRepository;
import br.com.api.satireapi.domain.customer.internal.usecase.account.CustomerAccountNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCustomerAddressUseCase {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public CreateCustomerAddressUseCase(
        CustomerRepository customerRepository,
        AddressRepository addressRepository
    ) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public CustomerAddressResponse execute(UUID customerId, CreateCustomerAddressRequest request) {
        requireActiveCustomer(customerId);
        var addresses = addressRepository.findAllByCustomerIdForUpdate(customerId);
        var primary = Boolean.TRUE.equals(request.primary()) || addresses.isEmpty();
        if (primary) {
            addresses.forEach(address -> address.markPrimary(false));
        }
        var address = addressRepository.save(Address.create(
            customerId,
            request.label(),
            request.recipient(),
            request.postalCode(),
            request.street(),
            request.number(),
            request.complement(),
            request.neighborhood(),
            request.city(),
            request.state(),
            primary
        ));
        return AddressMapper.toResponse(address);
    }

    private void requireActiveCustomer(UUID customerId) {
        customerRepository.findByIdForUpdate(customerId)
            .filter(customer -> customer.isActive())
            .orElseThrow(CustomerAccountNotFoundException::new);
    }
}
