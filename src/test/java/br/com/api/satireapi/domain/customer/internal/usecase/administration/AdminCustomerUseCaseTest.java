package br.com.api.satireapi.domain.customer.internal.usecase.administration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.customer.internal.dto.request.AdminCustomerFilter;
import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateAdminCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.model.Address;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class AdminCustomerUseCaseTest {

    private final AdminCustomerQuery adminCustomerQuery = mock(AdminCustomerQuery.class);

    @Test
    void listsCustomersUsingRequestedFiltersAndPagination() {
        var customer = Customer.register(
            "Cliente",
            "cliente@example.com",
            "password-hash",
            null,
            null
        );
        var filter = new AdminCustomerFilter(" cliente ", true, "cliente");
        var pageable = PageRequest.of(0, 20);
        when(adminCustomerQuery.findAll(filter, pageable)).thenReturn(new PageImpl<>(List.of(customer)));

        var result = new ListAdminCustomersUseCase(adminCustomerQuery).execute(filter, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("cliente@example.com", result.getContent().get(0).email());
        assertEquals("CLIENTE", filter.profile());
    }

    @Test
    void getsCustomerDetailsWithProfilesAndAddresses() {
        var customerId = UUID.randomUUID();
        var customer = Customer.register(
            "Cliente",
            "cliente@example.com",
            "password-hash",
            "12345678901",
            "11999999999"
        );
        var profile = mock(Profile.class);
        var address = mock(Address.class);
        when(profile.getName()).thenReturn("CLIENTE");
        customer.assignProfile(profile);
        when(adminCustomerQuery.findById(customerId)).thenReturn(Optional.of(customer));
        when(adminCustomerQuery.findAddressesByCustomerId(customerId)).thenReturn(List.of(address));

        var response = new GetAdminCustomerUseCase(adminCustomerQuery).execute(customerId);

        assertEquals("Cliente", response.name());
        assertEquals(java.util.Set.of("CLIENTE"), response.profiles());
        assertEquals(1, response.addresses().size());
    }

    @Test
    void updatesAdministrativeCustomerData() {
        var customerId = UUID.randomUUID();
        var customer = Customer.register(
            "Cliente",
            "cliente@example.com",
            "password-hash",
            null,
            null
        );
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));

        new UpdateAdminCustomerUseCase(adminCustomerQuery).execute(
            customerId,
            new UpdateAdminCustomerRequest("Cliente Atualizado", "NOVO@EXAMPLE.COM", "", "11888888888")
        );

        assertEquals("Cliente Atualizado", customer.getName());
        assertEquals("novo@example.com", customer.getEmail());
        assertEquals(null, customer.getCpf());
        assertEquals("11888888888", customer.getPhone());
    }

    @Test
    void changesCustomerStatus() {
        var customerId = UUID.randomUUID();
        var customer = Customer.register(
            "Cliente",
            "cliente@example.com",
            "password-hash",
            null,
            null
        );
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));

        new ChangeAdminCustomerStatusUseCase(adminCustomerQuery).execute(customerId, false);

        assertFalse(customer.isActive());
    }

    @Test
    void assignsProfileIdempotentlyThroughTheDomainModel() {
        var customerId = UUID.randomUUID();
        var customer = mock(Customer.class);
        var profile = mock(Profile.class);
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(adminCustomerQuery.findProfileByName("ADMIN")).thenReturn(Optional.of(profile));

        new AssignCustomerProfileUseCase(adminCustomerQuery).execute(customerId, "ADMIN");

        verify(customer).assignProfile(profile);
    }

    @Test
    void rejectsRemovalOfLastAdmin() {
        var customerId = UUID.randomUUID();
        var customer = mock(Customer.class);
        var profile = mock(Profile.class);
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(adminCustomerQuery.findProfileByNameForUpdate("ADMIN")).thenReturn(Optional.of(profile));
        when(customer.hasProfile("ADMIN")).thenReturn(true);
        when(adminCustomerQuery.countByProfileName("ADMIN")).thenReturn(1L);

        assertThrows(
            LastAdminRemovalNotAllowedException.class,
            () -> new RemoveCustomerProfileUseCase(adminCustomerQuery).execute(customerId, "ADMIN")
        );
        verify(customer, never()).removeProfile(any());
    }

    @Test
    void removesAdminProfileWhenAnotherAdminExists() {
        var customerId = UUID.randomUUID();
        var customer = mock(Customer.class);
        var profile = mock(Profile.class);
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.of(customer));
        when(adminCustomerQuery.findProfileByNameForUpdate("ADMIN")).thenReturn(Optional.of(profile));
        when(customer.hasProfile("ADMIN")).thenReturn(true);
        when(adminCustomerQuery.countByProfileName("ADMIN")).thenReturn(2L);

        new RemoveCustomerProfileUseCase(adminCustomerQuery).execute(customerId, "ADMIN");

        verify(customer).removeProfile(profile);
    }

    @Test
    void returnsNotFoundWhenAdministrativeTargetDoesNotExist() {
        var customerId = UUID.randomUUID();
        when(adminCustomerQuery.findByIdForUpdate(customerId)).thenReturn(Optional.empty());

        assertThrows(
            AdminCustomerNotFoundException.class,
            () -> new ChangeAdminCustomerStatusUseCase(adminCustomerQuery).execute(customerId, false)
        );
    }
}
