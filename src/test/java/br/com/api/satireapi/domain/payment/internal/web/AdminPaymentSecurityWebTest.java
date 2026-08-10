package br.com.api.satireapi.domain.payment.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentRefundResponse;
import br.com.api.satireapi.domain.payment.internal.model.RefundStatus;
import br.com.api.satireapi.domain.payment.internal.usecase.refund.RequestPaymentRefundUseCase;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminPaymentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class AdminPaymentSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private RequestPaymentRefundUseCase requestPaymentRefundUseCase;

    @Test
    void refundRouteRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/admin/payments/" + UUID.randomUUID() + "/refund"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void customerProfileCannotRequestRefund() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/payments/" + UUID.randomUUID() + "/refund")
                .header("Authorization", bearer(customerId, "CLIENTE")))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanRequestRefundWithIdempotencyKey() throws Exception {
        var adminId = UUID.randomUUID();
        when(requestPaymentRefundUseCase.execute(any(), any(), any(), any())).thenReturn(
            new PaymentRefundResponse(
                UUID.randomUUID(), UUID.randomUUID(), RefundStatus.SOLICITADO,
                new BigDecimal("149.90"), "Cliente solicitou estorno", OffsetDateTime.now()
            )
        );

        mockMvc.perform(post("/api/v1/admin/payments/" + UUID.randomUUID() + "/refund")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .header("Idempotency-Key", "refund-key-123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Cliente solicitou estorno\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    void refundRequiresIdempotencyKey() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/payments/" + UUID.randomUUID() + "/refund")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"Cliente solicitou estorno\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void refundRequiresReason() throws Exception {
        var adminId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/admin/payments/" + UUID.randomUUID() + "/refund")
                .header("Authorization", bearer(adminId, "ADMIN"))
                .header("Idempotency-Key", "refund-key-123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    private String bearer(UUID customerId, String profile) {
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, Set.of(profile))));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "admin@example.com", List.of(profile)
        );
    }
}
