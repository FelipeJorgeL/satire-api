package br.com.api.satireapi.domain.customer.internal.mapper;

import java.util.stream.Collectors;

import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerResponse;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer customer) {
        var profiles = customer.getProfiles().stream()
            .map(Profile::getName)
            .collect(Collectors.toUnmodifiableSet());

        return new CustomerResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getCpf(),
            customer.getPhone(),
            customer.isActive(),
            profiles,
            customer.getCreatedAt()
        );
    }
}
