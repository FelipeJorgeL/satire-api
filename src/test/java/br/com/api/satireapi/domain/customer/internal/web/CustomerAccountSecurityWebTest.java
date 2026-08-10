package br.com.api.satireapi.domain.customer.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.account.DeactivateCustomerAccountUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.account.UpdateCustomerAccountUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.CreateCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.CustomerAddressNotFoundException;
import br.com.api.satireapi.domain.customer.internal.usecase.address.DeleteCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.GetCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.ListCustomerAddressesUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.SetPrimaryCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.UpdateCustomerAddressUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerAccountController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerAccountSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private UpdateCustomerAccountUseCase updateCustomerAccountUseCase;

    @MockitoBean
    private DeactivateCustomerAccountUseCase deactivateCustomerAccountUseCase;

    @MockitoBean
    private ListCustomerAddressesUseCase listCustomerAddressesUseCase;

    @MockitoBean
    private GetCustomerAddressUseCase getCustomerAddressUseCase;

    @MockitoBean
    private CreateCustomerAddressUseCase createCustomerAddressUseCase;

    @MockitoBean
    private UpdateCustomerAddressUseCase updateCustomerAddressUseCase;

    @MockitoBean
    private DeleteCustomerAddressUseCase deleteCustomerAddressUseCase;

    @MockitoBean
    private SetPrimaryCustomerAddressUseCase setPrimaryCustomerAddressUseCase;

    @Test
    void accountRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/me/addresses"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerCanUpdateAccount() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/me")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType("application/json")
                .content("{\"name\":\"Felipe Atualizado\",\"phone\":\"11999999999\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void emptyAccountUpdateIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/me")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType("application/json")
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void authenticatedCustomerCanListAddresses() throws Exception {
        var customerId = UUID.randomUUID();
        when(listCustomerAddressesUseCase.execute(customerId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/me/addresses")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isOk());
    }

    @Test
    void authenticatedCustomerCanCreateAddress() throws Exception {
        var customerId = UUID.randomUUID();
        when(createCustomerAddressUseCase.execute(any(), any())).thenReturn(addressResponse());

        mockMvc.perform(post("/api/v1/me/addresses")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType("application/json")
                .content("""
                    {
                      "recipient":"Felipe Jorge",
                      "postalCode":"01001000",
                      "street":"Praça da Sé",
                      "number":"1",
                      "neighborhood":"Sé",
                      "city":"São Paulo",
                      "state":"SP"
                    }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    void addressOfAnotherCustomerIsNotExposed() throws Exception {
        var customerId = UUID.randomUUID();
        var addressId = UUID.randomUUID();
        when(getCustomerAddressUseCase.execute(customerId, addressId))
            .thenThrow(new CustomerAddressNotFoundException());

        mockMvc.perform(get("/api/v1/me/addresses/" + addressId)
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isNotFound());
    }

    @Test
    void authenticatedCustomerCanDeactivateAccount() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/me")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isNoContent());
    }

    @Test
    void invalidAddressPayloadIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/me/addresses")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType("application/json")
                .content("{\"recipient\":\"Felipe\"}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(),
            "felipe@example.com",
            List.of(profile)
        );
    }

    private CustomerAddressResponse addressResponse() {
        return new CustomerAddressResponse(
            UUID.randomUUID(), "Casa", "Felipe Jorge", "01001000", "Praça da Sé", "1",
            null, "Sé", "São Paulo", "SP", true, OffsetDateTime.now()
        );
    }
}
