package br.com.api.satireapi.domain.order.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.order.internal.usecase.review.DeleteCustomerReviewUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.review.UpsertCustomerReviewUseCase;
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

@WebMvcTest(controllers = CustomerReviewController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerReviewSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @MockitoBean
    private UpsertCustomerReviewUseCase upsertCustomerReviewUseCase;

    @MockitoBean
    private DeleteCustomerReviewUseCase deleteCustomerReviewUseCase;

    @Test
    void reviewWritesRequireAuthentication() throws Exception {
        var productId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/me/reviews/" + productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/v1/me/reviews/" + productId))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedCustomerCanUpsertReview() throws Exception {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        when(upsertCustomerReviewUseCase.execute(any(), any(), any()))
            .thenReturn(review(productId));

        mockMvc.perform(put("/api/v1/me/reviews/" + productId)
                .header("Authorization", bearer(customerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5,\"comment\":\"Excelente\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.productId").value(productId.toString()))
            .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void invalidRatingIsRejected() throws Exception {
        var customerId = UUID.randomUUID();

        mockMvc.perform(put("/api/v1/me/reviews/" + UUID.randomUUID())
                .header("Authorization", bearer(customerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":6}"))
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

    private ProductReview review(UUID productId) {
        return new ProductReview(
            UUID.randomUUID(), productId, (short) 5, "Excelente",
            OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
