package br.com.api.satireapi.domain.customer.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;

class AuthenticateCustomerUseCaseTest {

    private final CustomerFinder customerFinder = mock(CustomerFinder.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final LoginAttemptTracker loginAttemptTracker = mock(LoginAttemptTracker.class);
    private final RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
    private final OpaqueTokenGenerator tokenGenerator = new OpaqueTokenGenerator();
    private final AuthenticateCustomerUseCase useCase = new AuthenticateCustomerUseCase(
        customerFinder,
        passwordEncoder,
        jwtTokenService,
        loginAttemptTracker,
        refreshTokenStore,
        tokenGenerator,
        2_592_000L
    );

    @Test
    void authenticatesWithValidCredentials() {
        var customerId = UUID.randomUUID();
        var profile = mock(Profile.class);
        when(profile.getName()).thenReturn("CLIENTE");

        var customer = mock(Customer.class);
        when(customer.isActive()).thenReturn(true);
        when(customer.getPasswordHash()).thenReturn("encoded-password");
        when(customer.getId()).thenReturn(customerId);
        when(customer.getEmail()).thenReturn("felipe@example.com");
        when(customer.getProfiles()).thenReturn(new LinkedHashSet<>(List.of(profile)));

        when(customerFinder.findByEmail("felipe@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("safe-password", "encoded-password")).thenReturn(true);
        when(jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE")))
            .thenReturn("jwt-token");
        when(jwtTokenService.expirationSeconds()).thenReturn(3600L);

        var response = useCase.execute(new LoginRequest(" FELIPE@EXAMPLE.COM ", "safe-password"));

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
        verify(loginAttemptTracker).recordSuccess("felipe@example.com");
    }

    @Test
    void rejectsWrongPassword() {
        var customer = mock(Customer.class);
        when(customer.isActive()).thenReturn(true);
        when(customer.getPasswordHash()).thenReturn("encoded-password");

        when(customerFinder.findByEmail("felipe@example.com")).thenReturn(Optional.of(customer));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> useCase.execute(new LoginRequest("felipe@example.com", "wrong-password")));
        verify(loginAttemptTracker).recordFailure("felipe@example.com");
    }

    @Test
    void rejectsUnknownEmail() {
        when(customerFinder.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
            () -> useCase.execute(new LoginRequest("ghost@example.com", "any-password")));

        // Custo de BCrypt deve ser pago mesmo sem conta: bloqueia enumeração por timing.
        verify(passwordEncoder).matches(eq("any-password"), any());
        // Falha em e-mail inexistente também conta: o 429 não pode virar oráculo de existência.
        verify(loginAttemptTracker).recordFailure("ghost@example.com");
    }

    @Test
    void blocksLoginWhenAttemptsExceeded() {
        when(loginAttemptTracker.isBlocked("felipe@example.com")).thenReturn(true);

        assertThrows(TooManyLoginAttemptsException.class,
            () -> useCase.execute(new LoginRequest("felipe@example.com", "safe-password")));
        verifyNoInteractions(customerFinder);
    }

    @Test
    void rejectsInactiveCustomer() {
        var customer = mock(Customer.class);
        when(customer.isActive()).thenReturn(false);

        when(customerFinder.findByEmail("felipe@example.com")).thenReturn(Optional.of(customer));

        assertThrows(InvalidCredentialsException.class,
            () -> useCase.execute(new LoginRequest("felipe@example.com", "safe-password")));
    }
}
