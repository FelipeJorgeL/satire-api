package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.mapper.AddressMapper;
import br.com.api.satireapi.domain.customer.internal.persistence.AddressRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCustomerAddressUseCase {

    private final AddressRepository addressRepository;

    public GetCustomerAddressUseCase(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public CustomerAddressResponse execute(UUID customerId, UUID addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
            .map(AddressMapper::toResponse)
            .orElseThrow(CustomerAddressNotFoundException::new);
    }
}
