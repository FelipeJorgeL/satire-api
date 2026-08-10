package br.com.api.satireapi.domain.order.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetAdminOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListAdminOrdersUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.status.ChangeAdminOrderStatusUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.math.BigDecimal;
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

@WebMvcTest(controllers = AdminOrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminOrderSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private ListAdminOrdersUseCase listAdminOrdersUseCase;

    @MockitoBean
    private GetAdminOrderUseCase getAdminOrderUseCase;

    @MockitoBean
    private ChangeAdminOrderStatusUseCase changeAdminOrderStatusUseCase;

    @Test
    void orderAdminRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerProfileCannotAccessOrderAdminRoutes() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/admin/orders")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndInspectOrders() throws Exception {
        var adminId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(listAdminOrdersUseCase.execute(any(), any())).thenReturn(Page.empty());
        when(getAdminOrderUseCase.execute(orderId)).thenReturn(details(orderId));

        mockMvc.perform(get("/api/v1/admin/orders?status=PAGO")
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/admin/orders/" + orderId)
                .header("Authorization", bearer(adminId, "ADMIN")))
            .andExpect(status().isOk());
    }

    @Test
    void adminCanChangeOrderStatus() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/orders/" + UUID.randomUUID() + "/status")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"EM_SEPARACAO\"}"))
            .andExpect(status().isNoContent());
    }

    @Test
    void cancellationRequiresReason() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/admin/orders/" + UUID.randomUUID() + "/status")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELADO\"}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "admin@example.com", List.of(profile)
        );
    }

    private AdminOrderDetailsResponse details(UUID orderId) {
        return new AdminOrderDetailsResponse(
            orderId, UUID.randomUUID(), "SAT-0001", OrderStatus.PAGO,
            BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.TEN,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of(), null, List.of()
        );
    }
}
