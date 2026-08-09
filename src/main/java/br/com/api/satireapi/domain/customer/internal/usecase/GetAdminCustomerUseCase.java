package br.com.api.satireapi.domain.customer.internal.usecase;

import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerDetailsResponse;
import br.com.api.satireapi.domain.customer.internal.model.Address;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import java.util.Collections;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminCustomerUseCase {

    private final AdminCustomerQuery adminCustomerQuery;

    public GetAdminCustomerUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional(readOnly = true)
    public AdminCustomerDetailsResponse execute(UUID customerId) {
        var customer = adminCustomerQuery.findById(customerId)
            .orElseThrow(AdminCustomerNotFoundException::new);
        var profiles = customer.getProfiles().stream()
            .map(Profile::getName)
            .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        var addresses = adminCustomerQuery.findAddressesByCustomerId(customerId).stream()
            .map(GetAdminCustomerUseCase::toAddressResponse)
            .toList();

        return new AdminCustomerDetailsResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getCpf(),
            customer.getPhone(),
            customer.isActive(),
            Collections.unmodifiableSet(profiles),
            addresses,
            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }

    private static AdminCustomerAddressResponse toAddressResponse(Address address) {
        return new AdminCustomerAddressResponse(
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
