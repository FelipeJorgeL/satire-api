package br.com.api.satireapi.domain.customer.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerDetailsResponse;
import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.customer.internal.usecase.AssignCustomerProfileUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.ChangeAdminCustomerStatusUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.GetAdminCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.ListAdminCustomersUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.LastAdminRemovalNotAllowedException;
import br.com.api.satireapi.domain.customer.internal.usecase.RemoveCustomerProfileUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.UpdateAdminCustomerUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminCustomerController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminCustomerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private ListAdminCustomersUseCase listAdminCustomersUseCase;

    @MockitoBean
    private GetAdminCustomerUseCase getAdminCustomerUseCase;

    @MockitoBean
    private UpdateAdminCustomerUseCase updateAdminCustomerUseCase;

    @MockitoBean
    private ChangeAdminCustomerStatusUseCase changeAdminCustomerStatusUseCase;

    @MockitoBean
    private AssignCustomerProfileUseCase assignCustomerProfileUseCase;

    @MockitoBean
    private RemoveCustomerProfileUseCase removeCustomerProfileUseCase;

    @Test
    void adminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminRoutesRejectCustomerProfile() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer("CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void inactiveCustomerTokenIsRejected() throws Exception {
        var customerId = UUID.randomUUID();
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(java.util.Optional.of(new CustomerAuthentication(customerId, false, Set.of("ADMIN"))));

        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", tokenFor(customerId, "ADMIN")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void removedAdminProfileIsRejectedEvenWithOldToken() throws Exception {
        var customerId = UUID.randomUUID();
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(java.util.Optional.of(new CustomerAuthentication(customerId, true, Set.of("CLIENTE"))));

        mockMvc.perform(get("/api/v1/admin/users")
                .header("Authorization", tokenFor(customerId, "ADMIN")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListCustomers() throws Exception {
        when(listAdminCustomersUseCase.execute(any(), any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/admin/users").header("Authorization", bearer("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void adminCanGetCustomerDetails() throws Exception {
        var customerId = UUID.randomUUID();
        when(getAdminCustomerUseCase.execute(customerId)).thenReturn(new AdminCustomerDetailsResponse(
            customerId,
            "Cliente",
            "cliente@example.com",
            null,
            null,
            true,
            Set.of("CLIENTE"),
            List.of(),
            OffsetDateTime.now(),
            OffsetDateTime.now()
        ));

        mockMvc.perform(get("/api/v1/admin/users/" + customerId).header("Authorization", bearer("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void adminCanUpdateCustomer() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/" + customerId)
                .header("Authorization", bearer("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Cliente Atualizado\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanChangeCustomerStatus() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/" + customerId + "/status")
                .header("Authorization", bearer("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":false}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanAssignProfile() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/admin/users/" + customerId + "/profiles/ADMIN")
                .header("Authorization", bearer("ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void adminCanRemoveProfile() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/admin/users/" + customerId + "/profiles/CLIENTE")
                .header("Authorization", bearer("ADMIN")))
            .andExpect(status().isNoContent());
    }

    @Test
    void emptyAdministrativeUpdateIsBadRequest() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/users/" + customerId)
                .header("Authorization", bearer("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void invalidProfileIsBadRequest() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/admin/users/" + customerId + "/profiles/SUPER_ADMIN")
                .header("Authorization", bearer("ADMIN")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void removingLastAdminReturnsConflict() throws Exception {
        var customerId = UUID.randomUUID();
        doThrow(new LastAdminRemovalNotAllowedException())
            .when(removeCustomerProfileUseCase).execute(customerId, "ADMIN");

        mockMvc.perform(delete("/api/v1/admin/users/" + customerId + "/profiles/ADMIN")
                .header("Authorization", bearer("ADMIN")))
            .andExpect(status().isConflict());
    }

    private String bearer(String profile) {
        return bearerFor(UUID.randomUUID(), profile);
    }

    private String bearerFor(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(java.util.Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return tokenFor(customerId, profile);
    }

    private String tokenFor(UUID customerId, String profile) {
        var token = jwtTokenService.generate(
            customerId.toString(),
            "admin@example.com",
            List.of(profile)
        );
        return "Bearer " + token;
    }
}
