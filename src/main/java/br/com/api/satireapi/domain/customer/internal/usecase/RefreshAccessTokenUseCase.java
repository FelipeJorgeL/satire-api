package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;

@Service
public class RefreshAccessTokenUseCase {

    private final RefreshTokenStore refreshTokenStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final CustomerFinder customerFinder;
    private final JwtTokenService jwtTokenService;
    private final long refreshExpirationSeconds;

    public RefreshAccessTokenUseCase(
        RefreshTokenStore refreshTokenStore,
        OpaqueTokenGenerator tokenGenerator,
        CustomerFinder customerFinder,
        JwtTokenService jwtTokenService,
        @Value("${app.jwt.refresh-expiration}") long refreshExpirationSeconds
    ) {
        this.refreshTokenStore = refreshTokenStore;
        this.tokenGenerator = tokenGenerator;
        this.customerFinder = customerFinder;
        this.jwtTokenService = jwtTokenService;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    @Transactional
    public LoginResponse execute(RefreshTokenRequest request) {
        var tokenHash = tokenGenerator.hash(request.refreshToken());
        var customerId = refreshTokenStore.findCustomerIdByHash(tokenHash, Instant.now())
            .orElseThrow(InvalidRefreshTokenException::new);

        var customer = customerFinder.findById(customerId)
            .filter(Customer::isActive)
            .orElseThrow(InvalidRefreshTokenException::new);

        // Rotação: o token usado deixa de valer, mesmo se o cliente perder a resposta desta chamada.
        var rawRefreshToken = tokenGenerator.generate();
        var refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        refreshTokenStore.save(customer.getId(), tokenGenerator.hash(rawRefreshToken), refreshExpiresAt);

        var profiles = customer.getProfiles().stream().map(Profile::getName).toList();
        var accessToken = jwtTokenService.generate(customer.getId().toString(), customer.getEmail(), profiles);

        return new LoginResponse(
            accessToken,
            "Bearer",
            jwtTokenService.expirationSeconds(),
            rawRefreshToken,
            refreshExpirationSeconds
        );
    }
}
