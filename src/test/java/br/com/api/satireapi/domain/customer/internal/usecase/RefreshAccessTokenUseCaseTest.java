package br.com.api.satireapi.domain.customer.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;

class RefreshAccessTokenUseCaseTest {

    private final RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
    private final OpaqueTokenGenerator tokenGenerator = new OpaqueTokenGenerator();
    private final CustomerFinder customerFinder = mock(CustomerFinder.class);
    private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
    private final RefreshAccessTokenUseCase useCase = new RefreshAccessTokenUseCase(
        refreshTokenStore,
        tokenGenerator,
        customerFinder,
        jwtTokenService,
        2_592_000L
    );

    @Test
    void issuesNewAccessTokenAndRotatesRefreshTokenForValidToken() {
        var customerId = UUID.randomUUID();
        var rawToken = "valid-refresh-token";
        var tokenHash = tokenGenerator.hash(rawToken);

        var profile = mock(Profile.class);
        when(profile.getName()).thenReturn("CLIENTE");
        var customer = mock(Customer.class);
        when(customer.isActive()).thenReturn(true);
        when(customer.getId()).thenReturn(customerId);
        when(customer.getEmail()).thenReturn("felipe@example.com");
        when(customer.getProfiles()).thenReturn(new LinkedHashSet<>(List.of(profile)));

        when(refreshTokenStore.findCustomerIdByHash(eq(tokenHash), any(Instant.class)))
            .thenReturn(Optional.of(customerId));
        when(customerFinder.findById(customerId)).thenReturn(Optional.of(customer));
        when(jwtTokenService.generate(customerId.toString(), "felipe@example.com", List.of("CLIENTE")))
            .thenReturn("new-access-token");
        when(jwtTokenService.expirationSeconds()).thenReturn(3600L);

        var response = useCase.execute(new RefreshTokenRequest(rawToken));

        assertEquals("new-access-token", response.accessToken());
        assertEquals(3600L, response.expiresIn());
        verify(refreshTokenStore).save(eq(customerId), any(), any());
    }

    @Test
    void rejectsUnknownOrExpiredToken() {
        when(refreshTokenStore.findCustomerIdByHash(any(), any())).thenReturn(Optional.empty());

        assertThrows(InvalidRefreshTokenException.class,
            () -> useCase.execute(new RefreshTokenRequest("unknown-token")));
        verify(refreshTokenStore, never()).save(any(), any(), any());
    }

    @Test
    void rejectsTokenForInactiveCustomer() {
        var customerId = UUID.randomUUID();
        var customer = mock(Customer.class);
        when(customer.isActive()).thenReturn(false);

        when(refreshTokenStore.findCustomerIdByHash(any(), any())).thenReturn(Optional.of(customerId));
        when(customerFinder.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(InvalidRefreshTokenException.class,
            () -> useCase.execute(new RefreshTokenRequest("some-token")));
    }
}
