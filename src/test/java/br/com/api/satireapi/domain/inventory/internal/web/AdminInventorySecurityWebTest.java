package br.com.api.satireapi.domain.inventory.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockMovementResponse;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockVariationResponse;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import br.com.api.satireapi.domain.inventory.internal.usecase.movement.ListAdminStockMovementsUseCase;
import br.com.api.satireapi.domain.inventory.internal.usecase.movement.RegisterStockMovementUseCase;
import br.com.api.satireapi.domain.inventory.internal.usecase.variation.GetAdminStockVariationUseCase;
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
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminInventoryController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminInventorySecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private RegisterStockMovementUseCase registerStockMovementUseCase;

    @MockitoBean
    private ListAdminStockMovementsUseCase listAdminStockMovementsUseCase;

    @MockitoBean
    private GetAdminStockVariationUseCase getAdminStockVariationUseCase;

    @Test
    void inventoryAdminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/inventory/movements"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerProfileCannotAccessInventoryAdminRoutes() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/inventory/movements")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanRegisterMovement() throws Exception {
        var adminId = UUID.randomUUID();
        when(registerStockMovementUseCase.execute(any(), any())).thenReturn(
            new AdminStockMovementResponse(
                UUID.randomUUID(), UUID.randomUUID(), adminId, StockMovementType.ENTRADA,
                5, 10, 15, "Reposição", OffsetDateTime.now()
            )
        );

        mockMvc.perform(post("/api/v1/admin/inventory/movements")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "variationId":"%s",
                      "type":"ENTRADA",
                      "quantity":5,
                      "observation":"Reposição"
                    }
                    """.formatted(UUID.randomUUID())))
            .andExpect(status().isCreated());
    }

    @Test
    void invalidMovementQuantityIsRejected() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/inventory/movements")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "variationId":"%s",
                      "type":"SAIDA",
                      "quantity":0
                    }
                    """.formatted(UUID.randomUUID())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanListMovementsAndInspectVariationStock() throws Exception {
        var adminId = UUID.randomUUID();
        when(listAdminStockMovementsUseCase.execute(any(), any())).thenReturn(Page.empty());
        when(getAdminStockVariationUseCase.execute(any())).thenReturn(
            new AdminStockVariationResponse(
                UUID.randomUUID(), UUID.randomUUID(), "SKU-1", "Variação", true,
                15, 20, 5, List.of()
            )
        );

        mockMvc.perform(get("/api/v1/admin/inventory/movements")
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/inventory/variations/" + UUID.randomUUID())
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isOk());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "admin@example.com", List.of(profile)
        );
    }
}
