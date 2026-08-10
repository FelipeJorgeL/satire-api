package br.com.api.satireapi.domain.shipping.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.shipping.internal.dto.response.AdminShipmentResponse;
import br.com.api.satireapi.domain.shipping.internal.model.ShippingStatus;
import br.com.api.satireapi.domain.shipping.internal.usecase.details.CompleteShipmentDetailsUseCase;
import br.com.api.satireapi.domain.shipping.internal.usecase.status.ChangeShipmentStatusUseCase;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminShippingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminShippingSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private CompleteShipmentDetailsUseCase completeShipmentDetailsUseCase;

    @MockitoBean
    private ChangeShipmentStatusUseCase changeShipmentStatusUseCase;

    @Test
    void shippingAdminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/admin/orders/" + UUID.randomUUID() + "/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carrier\":\"Correios\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerProfileCannotAccessShippingAdminRoutes() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/shipments/" + UUID.randomUUID() + "/status")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ENVIADO\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCompleteShipmentDetails() throws Exception {
        var adminId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(completeShipmentDetailsUseCase.execute(any(), any())).thenReturn(response(orderId));

        mockMvc.perform(post("/api/v1/admin/orders/" + orderId + "/shipping")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carrier\":\"Correios\",\"trackingCode\":\"BR123456789\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void adminCanChangeShipmentStatus() throws Exception {
        var adminId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(changeShipmentStatusUseCase.execute(any(), any())).thenReturn(response(orderId));

        mockMvc.perform(patch("/api/v1/admin/shipments/" + UUID.randomUUID() + "/status")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ENVIADO\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void shipmentDetailsRequireAtLeastOneValue() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/orders/" + UUID.randomUUID() + "/shipping")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "admin@example.com", List.of(profile)
        );
    }

    private AdminShipmentResponse response(UUID orderId) {
        return new AdminShipmentResponse(
            UUID.randomUUID(), orderId, "Correios", "BR123456789", ShippingStatus.ENVIADO,
            OffsetDateTime.now(), null, null, OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
