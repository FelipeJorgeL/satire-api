package br.com.api.satireapi.domain.customer.internal.usecase.address;

import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.mapper.AddressMapper;
import br.com.api.satireapi.domain.customer.internal.persistence.AddressRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCustomerAddressesUseCase {

    private final AddressRepository addressRepository;

    public ListCustomerAddressesUseCase(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> execute(UUID customerId) {
        return addressRepository.findAllByCustomerIdOrderByPrimaryDescCreatedAtAsc(customerId)
            .stream()
            .map(AddressMapper::toResponse)
            .toList();
    }
}
