package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;

public class RefreshAccessTokenUseCase {

    private final RefreshTokenStore refreshTokenStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final CustomerFinder customerFinder;
    private final AccessTokenIssuer accessTokenIssuer;
    private final long refreshExpirationSeconds;

    public RefreshAccessTokenUseCase(
        RefreshTokenStore refreshTokenStore,
        OpaqueTokenGenerator tokenGenerator,
        CustomerFinder customerFinder,
        AccessTokenIssuer accessTokenIssuer,
        @Value("${app.jwt.refresh-expiration}") long refreshExpirationSeconds
    ) {
        this.refreshTokenStore = refreshTokenStore;
        this.tokenGenerator = tokenGenerator;
        this.customerFinder = customerFinder;
        this.accessTokenIssuer = accessTokenIssuer;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    @Transactional
    public LoginResponse execute(RefreshTokenRequest request) {
        if (request.refreshToken().length() > OpaqueTokenGenerator.MAX_TOKEN_LENGTH) {
            throw new InvalidRefreshTokenException();
        }
        var tokenHash = tokenGenerator.hash(request.refreshToken());
        var customerId = refreshTokenStore.findCustomerIdByHash(tokenHash, Instant.now())
            .orElseThrow(InvalidRefreshTokenException::new);

        var customer = customerFinder.findById(customerId)
            .filter(Customer::isActive)
            .orElseThrow(InvalidRefreshTokenException::new);

        // Rotação: o token usado deixa de valer, mesmo se o cliente perder a resposta desta chamada.
        var rawRefreshToken = tokenGenerator.generate();
        var refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        if (!refreshTokenStore.rotateIfCurrent(
            customer.getId(), tokenHash, Instant.now(), tokenGenerator.hash(rawRefreshToken), refreshExpiresAt
        )) {
            throw new InvalidRefreshTokenException();
        }

        var profiles = customer.getProfiles().stream().map(Profile::getName).toList();
        var accessToken = accessTokenIssuer.generate(customer.getId().toString(), customer.getEmail(), profiles);

        return new LoginResponse(
            accessToken,
            "Bearer",
            accessTokenIssuer.expirationSeconds(),
            rawRefreshToken,
            refreshExpirationSeconds
        );
    }
}
