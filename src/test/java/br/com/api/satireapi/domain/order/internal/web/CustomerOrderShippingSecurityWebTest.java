package br.com.api.satireapi.domain.order.internal.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderShipmentUseCase;
import br.com.api.satireapi.domain.shipping.CustomerShipment;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;
import java.time.LocalDate;
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

@WebMvcTest(controllers = CustomerOrderShippingController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerOrderShippingSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private GetCustomerOrderShipmentUseCase getCustomerOrderShipmentUseCase;

    @Test
    void routeRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + UUID.randomUUID() + "/shipping"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerCanQueryOwnShipment() throws Exception {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(getCustomerOrderShipmentUseCase.execute(customerId, orderId))
            .thenReturn(response(orderId));

        mockMvc.perform(get("/api/v1/orders/" + orderId + "/shipping")
                .header("Authorization", bearer(customerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.orderId").value(orderId.toString()))
            .andExpect(jsonPath("$.status").value("EM_TRANSITO"));

        verify(getCustomerOrderShipmentUseCase).execute(customerId, orderId);
    }

    @Test
    void malformedOrderIdIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/not-a-uuid/shipping")
                .header("Authorization", bearer(customerId)))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId) {
        when(customerAuthenticationGateway.findById(customerId)).thenReturn(Optional.of(
            new CustomerAuthentication(customerId, true, Set.of("CLIENTE"))
        ));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "customer@example.com", List.of("CLIENTE")
        );
    }

    private CustomerShipment response(UUID orderId) {
        return new CustomerShipment(
            UUID.randomUUID(), orderId, "Correios", "BR123456789", "EM_TRANSITO",
            OffsetDateTime.now(), null, LocalDate.now().plusDays(2)
        );
    }
}
