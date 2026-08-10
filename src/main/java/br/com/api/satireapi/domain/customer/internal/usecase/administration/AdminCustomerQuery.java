package br.com.api.satireapi.domain.customer.internal.usecase.administration;

import br.com.api.satireapi.domain.customer.internal.dto.request.AdminCustomerFilter;
import br.com.api.satireapi.domain.customer.internal.model.Address;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCustomerQuery {

    Page<Customer> findAll(AdminCustomerFilter filter, Pageable pageable);

    Optional<Customer> findById(UUID customerId);

    Optional<Customer> findByIdForUpdate(UUID customerId);

    List<Address> findAddressesByCustomerId(UUID customerId);

    Optional<Profile> findProfileByName(String profileName);

    Optional<Profile> findProfileByNameForUpdate(String profileName);

    long countByProfileName(String profileName);
}
