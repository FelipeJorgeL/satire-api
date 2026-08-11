package br.com.api.satireapi.domain.customer.internal.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.api.satireapi.domain.customer.CustomerGateway;
import br.com.api.satireapi.domain.customer.CustomerAuthenticationGateway;
import br.com.api.satireapi.domain.customer.CustomerAuthentication;
import br.com.api.satireapi.domain.customer.dto.CustomerSummary;
import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.AuthenticateCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.ConfirmEmailUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerEmailAlreadyExistsException;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.InvalidCredentialsException;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.LogoutUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RefreshAccessTokenUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RegisterCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RegistrationRateLimiter;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.ResendConfirmationEmailUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.TooManyLoginAttemptsException;
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
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private ConfirmEmailUseCase confirmEmailUseCase;

    @MockitoBean
    private RegistrationRateLimiter registrationRateLimiter;

    @MockitoBean
    private ResendConfirmationEmailUseCase resendConfirmationEmailUseCase;

    @MockitoBean
    private CustomerGateway customerGateway;

    @MockitoBean
    private CustomerAuthenticationGateway customerAuthenticationGateway;

    @Test
    void registerReturnsSameGenericResponseForNewAndExistingEmail() throws Exception {
        when(registrationRateLimiter.allow(any(), any(), any())).thenReturn(true);
        var body = objectMapper.writeValueAsString(new RegisterCustomerRequest(
            "Felipe Jorge", "felipe@example.com", "safe-password", "12345678901", "11999999999"));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isAccepted())
            .andExpect(content().string(""));

        when(registerCustomerUseCase.execute(any())).thenThrow(new CustomerEmailAlreadyExistsException());

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isAccepted())
            .andExpect(content().string(""));
    }

    @Test
    void loginBlockedByRateLimitReturnsTooManyRequests() throws Exception {
        when(authenticateCustomerUseCase.execute(any(), any())).thenThrow(new TooManyLoginAttemptsException());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("felipe@example.com", "safe-password"))))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void loginWithValidCredentialsReturnsToken() throws Exception {
        when(authenticateCustomerUseCase.execute(any(), any())).thenReturn(new LoginResponse("jwt-token", "Bearer", 3600));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("felipe@example.com", "safe-password"))))
            .andExpect(status().isOk());
    }

    @Test
    void loginWithInvalidCredentialsReturnsUnauthorized() throws Exception {
        when(authenticateCustomerUseCase.execute(any(), any())).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("felipe@example.com", "wrong-password"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginRejectsPasswordAboveMaximumAtWebBoundaryWithoutInvokingAuthentication() throws Exception {
        var oversizedPassword = "p".repeat(73);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new LoginRequest("felipe@example.com", oversizedPassword))))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticateCustomerUseCase);
    }

    @Test
    void refreshIsPublicAndReturnsRotatedTokens() throws Exception {
        when(refreshAccessTokenUseCase.execute(any())).thenReturn(
            new LoginResponse("jwt-token", "Bearer", 3600, "refresh-token", 2592000)
        );

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"refresh-token\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void emailConfirmationRoutesArePublic() throws Exception {
        mockMvc.perform(get("/api/v1/auth/confirm?token=confirmation-token"))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/confirm?token=confirmation-token"))
            .andExpect(status().isOk());
    }

    @Test
    void resendConfirmationIsPublicAndGeneric() throws Exception {
        when(registrationRateLimiter.allow(any(), any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/confirm/resend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"felipe@example.com\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    void logoutRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
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
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, java.util.Set.of("CLIENTE"))));
        when(customerGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerSummary(customerId, "Felipe", "felipe@example.com", true)));

        mockMvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void customersEndpointRejectsClienteProfile() throws Exception {
        var customerId = UUID.randomUUID();
        var token = jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE"));
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, java.util.Set.of("CLIENTE"))));

        mockMvc.perform(get("/api/v1/customers/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void customersEndpointRequiresAdminForAnyMethod() throws Exception {
        var customerId = UUID.randomUUID();
        var token = jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE"));
        when(customerAuthenticationGateway.findById(customerId))
            .thenReturn(Optional.of(new CustomerAuthentication(customerId, true, java.util.Set.of("CLIENTE"))));

        mockMvc.perform(post("/api/v1/customers/" + UUID.randomUUID()).header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void customersEndpointAllowsAdminProfile() throws Exception {
        var adminId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var token = jwtTokenService.generate(adminId.toString(), "admin@example.com", List.of("ADMIN"));
        when(customerAuthenticationGateway.findById(adminId))
            .thenReturn(Optional.of(new CustomerAuthentication(adminId, true, java.util.Set.of("ADMIN"))));
        when(customerGateway.findById(targetId))
            .thenReturn(Optional.of(new CustomerSummary(targetId, "Cliente", "cliente@example.com", true)));

        mockMvc.perform(get("/api/v1/customers/" + targetId).header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
}
