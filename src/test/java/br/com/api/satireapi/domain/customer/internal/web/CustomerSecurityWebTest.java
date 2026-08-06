package br.com.api.satireapi.domain.customer.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.api.satireapi.domain.customer.CustomerGateway;
import br.com.api.satireapi.domain.customer.dto.CustomerSummary;
import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.AuthenticateCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.InvalidCredentialsException;
import br.com.api.satireapi.domain.customer.internal.usecase.RegisterCustomerUseCase;
import br.com.api.satireapi.infra.security.SecurityConfig;
import br.com.api.satireapi.infra.security.jwt.JwtAuthenticationFilter;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {CustomerController.class, CustomerQueryController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenService.class})
@TestPropertySource(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.jwt.expiration=3600"
})
class CustomerSecurityWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenService jwtTokenService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterCustomerUseCase registerCustomerUseCase;

    @MockitoBean
    private AuthenticateCustomerUseCase authenticateCustomerUseCase;

    @MockitoBean
    private CustomerGateway customerGateway;

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        when(authenticateCustomerUseCase.execute(any())).thenReturn(new LoginResponse("jwt-token", "Bearer", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("felipe@example.com", "safe-password"))))
            .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        when(authenticateCustomerUseCase.execute(any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("felipe@example.com", "wrong-password"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithValidTokenIsOk() throws Exception {
        var customerId = UUID.randomUUID();
        var token = jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE"));
        when(customerGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerSummary(customerId, "Felipe", "felipe@example.com", true)));

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void customersEndpointRejectsClienteProfile() throws Exception {
        var customerId = UUID.randomUUID();
        var token = jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE"));

        mockMvc.perform(get("/api/v1/customers/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void customersEndpointRequiresAdminForAnyMethod() throws Exception {
        var token = jwtTokenService.generate(UUID.randomUUID().toString(), "felipe@example.com", List.of("CLIENTE"));

        mockMvc.perform(post("/api/v1/customers/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void customersEndpointAllowsAdminProfile() throws Exception {
        var adminId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var token = jwtTokenService.generate(adminId.toString(), "admin@example.com", List.of("ADMIN"));
        when(customerGateway.findById(targetId))
            .thenReturn(Optional.of(new CustomerSummary(targetId, "Cliente", "cliente@example.com", true)));

        mockMvc.perform(get("/api/v1/customers/" + targetId).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
