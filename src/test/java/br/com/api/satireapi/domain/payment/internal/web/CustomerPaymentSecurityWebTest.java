package br.com.api.satireapi.domain.payment.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.model.PaymentMethod;
import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import br.com.api.satireapi.domain.payment.internal.usecase.creation.CreatePaymentUseCase;
import br.com.api.satireapi.domain.payment.internal.usecase.creation.PaymentCreationResult;
import br.com.api.satireapi.domain.payment.internal.usecase.query.GetPaymentUseCase;
import br.com.api.satireapi.domain.payment.internal.usecase.query.ListOrderPaymentsUseCase;
import br.com.api.satireapi.domain.payment.internal.usecase.webhook.ProcessPaymentWebhookUseCase;
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

@WebMvcTest(controllers = {
    CustomerOrderPaymentController.class,
    CustomerPaymentController.class,
    PaymentWebhookController.class
})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerPaymentSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private CreatePaymentUseCase createPaymentUseCase;

    @MockitoBean
    private ListOrderPaymentsUseCase listOrderPaymentsUseCase;

    @MockitoBean
    private GetPaymentUseCase getPaymentUseCase;

    @MockitoBean
    private ProcessPaymentWebhookUseCase webhookUseCase;

    @Test
    void customerPaymentRoutesRequireAuthentication() throws Exception {
        var orderId = UUID.randomUUID();
        var paymentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/orders/" + orderId + "/payments"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/payments/" + paymentId))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"PIX\"}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerCanCreateAndReadOwnPayments() throws Exception {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var payment = payment(orderId);
        when(createPaymentUseCase.execute(any(), any(), any(), any())).thenReturn(
            new PaymentCreationResult(payment, true)
        );
        when(listOrderPaymentsUseCase.execute(customerId, orderId)).thenReturn(List.of(payment));
        when(getPaymentUseCase.execute(customerId, payment.id())).thenReturn(payment);
        var authorization = bearer(customerId);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/payments")
                .header("Authorization", authorization)
                .header("Idempotency-Key", "payment-key-123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"PIX\"}"))
            .andExpect(status().isCreated());
        mockMvc.perform(get("/api/v1/orders/" + orderId + "/payments")
                .header("Authorization", authorization))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/payments/" + payment.id())
                .header("Authorization", authorization))
            .andExpect(status().isOk());

        verify(createPaymentUseCase).execute(
            org.mockito.ArgumentMatchers.eq(customerId),
            org.mockito.ArgumentMatchers.eq(orderId),
            org.mockito.ArgumentMatchers.eq("payment-key-123456"),
            any()
        );
    }

    @Test
    void idempotencyKeyAndMethodAreRequired() throws Exception {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var authorization = bearer(customerId);

        mockMvc.perform(post("/api/v1/orders/" + orderId + "/payments")
                .header("Authorization", authorization)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"method\":\"PIX\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/orders/" + orderId + "/payments")
                .header("Authorization", authorization)
                .header("Idempotency-Key", "payment-key-123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void webhookIsPublicOnlyAtSignedIntegrationRoute() throws Exception {
        var paymentId = UUID.randomUUID();
        var payload = "{\"status\":\"APROVADO\",\"gatewayTransactionId\":\"txn-1\"}";

        mockMvc.perform(post("/api/v1/payments/" + paymentId + "/webhook")
                .header("X-Webhook-Event-Id", "evt-12345678")
                .header("X-Webhook-Timestamp", "2026-08-11T12:00:00Z")
                .header("X-Webhook-Signature", "signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
            .andExpect(status().isNoContent());

        verify(webhookUseCase).execute(
            paymentId,
            "evt-12345678",
            "2026-08-11T12:00:00Z",
            "signature",
            payload
        );
    }

    @Test
    void malformedWebhookEventIdIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/payments/" + UUID.randomUUID() + "/webhook")
                .header("X-Webhook-Event-Id", "short")
                .header("X-Webhook-Timestamp", "2026-08-11T12:00:00Z")
                .header("X-Webhook-Signature", "signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void oversizedWebhookPayloadIsRejectedBeforeUseCaseExecution() throws Exception {
        mockMvc.perform(post("/api/v1/payments/" + UUID.randomUUID() + "/webhook")
                .header("X-Webhook-Event-Id", "evt-12345678")
                .header("X-Webhook-Timestamp", "2026-08-11T12:00:00Z")
                .header("X-Webhook-Signature", "signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content("x".repeat(8_193)))
            .andExpect(status().isBadRequest());

        verify(webhookUseCase, never()).execute(any(), any(), any(), any(), any());
    }

    private String bearer(UUID customerId) {
        when(customerAuthenticationGateway.findById(customerId)).thenReturn(Optional.of(
            new CustomerAuthentication(customerId, true, Set.of("CLIENTE"))
        ));
        return "Bearer " + jwtTokenService.generate(
            customerId.toString(), "customer@example.com", List.of("CLIENTE")
        );
    }

    private PaymentResponse payment(UUID orderId) {
        var now = OffsetDateTime.now();
        return new PaymentResponse(
            UUID.randomUUID(), orderId, PaymentMethod.PIX, PaymentStatus.PENDENTE,
            new BigDecimal("149.90"), null, null, now, now
        );
    }
}
