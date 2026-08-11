package br.com.api.satireapi.domain.order.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderResponse;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListCustomerOrderStatusHistoryUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListCustomerOrdersUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.creation.CreateOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.status.CancelCustomerOrderUseCase;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CustomerOrderController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerOrderSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private CreateOrderUseCase createOrderUseCase;

    @MockitoBean
    private ListCustomerOrdersUseCase listCustomerOrdersUseCase;

    @MockitoBean
    private GetCustomerOrderUseCase getCustomerOrderUseCase;

    @MockitoBean
    private ListCustomerOrderStatusHistoryUseCase listCustomerOrderStatusHistoryUseCase;

    @MockitoBean
    private CancelCustomerOrderUseCase cancelCustomerOrderUseCase;

    @Test
    void orderCreationRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"addressId\":\"" + UUID.randomUUID() + "\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCanCreateOrder() throws Exception {
        var customerId = UUID.randomUUID();
        var addressId = UUID.randomUUID();
        when(createOrderUseCase.execute(any(), any())).thenReturn(
            new CustomerOrderResponse(
                UUID.randomUUID(), "SAT-123", OrderStatus.AGUARDANDO_PAGAMENTO,
                BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("14.90"),
                new BigDecimal("24.90"), OffsetDateTime.now()
            )
        );

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"addressId\":\"" + addressId + "\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    void addressIsRequiredForOrderCreation() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/orders")
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void customerOrderReadsRequireAuthentication() throws Exception {
        var orderId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/orders/{orderId}/status-history", orderId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCanListOwnOrders() throws Exception {
        var customerId = UUID.randomUUID();
        var response = new CustomerOrderResponse(
            UUID.randomUUID(), "SAT-123", OrderStatus.AGUARDANDO_PAGAMENTO,
            BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("14.90"),
            new BigDecimal("24.90"), OffsetDateTime.now()
        );
        var pageable = PageRequest.of(0, 20);
        when(listCustomerOrdersUseCase.execute(any(), any(), any()))
            .thenReturn(new PageImpl<>(List.of(response), pageable, 1));

        mockMvc.perform(get("/api/v1/orders")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isOk());
    }

    @Test
    void customerCancellationRequiresAReason() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/orders/{orderId}/cancel", UUID.randomUUID())
                .header("Authorization", bearer(customerId, "CLIENTE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "customer@example.com", List.of(profile)
        );
    }
}
