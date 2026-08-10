package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateCustomerAddressRequest;
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
public class UpdateCustomerAddressUseCase {

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;

    public UpdateCustomerAddressUseCase(
        CustomerRepository customerRepository,
        AddressRepository addressRepository
    ) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public CustomerAddressResponse execute(
        UUID customerId,
        UUID addressId,
        UpdateCustomerAddressRequest request
    ) {
        requireActiveCustomer(customerId);
        var address = addressRepository.findByIdAndCustomerIdForUpdate(addressId, customerId)
            .orElseThrow(CustomerAddressNotFoundException::new);
        address.update(
            valueOrCurrent(request.label(), address.getLabel()),
            valueOrCurrent(request.recipient(), address.getRecipient()),
            valueOrCurrent(request.postalCode(), address.getPostalCode()),
            valueOrCurrent(request.street(), address.getStreet()),
            valueOrCurrent(request.number(), address.getNumber()),
            valueOrCurrent(request.complement(), address.getComplement()),
            valueOrCurrent(request.neighborhood(), address.getNeighborhood()),
            valueOrCurrent(request.city(), address.getCity()),
            valueOrCurrent(request.state(), address.getState())
        );
        return AddressMapper.toResponse(address);
    }

    private void requireActiveCustomer(UUID customerId) {
        customerRepository.findByIdForUpdate(customerId)
            .filter(customer -> customer.isActive())
            .orElseThrow(CustomerAccountNotFoundException::new);
    }

    private static String valueOrCurrent(String value, String current) {
        return value == null ? current : value;
    }
}
