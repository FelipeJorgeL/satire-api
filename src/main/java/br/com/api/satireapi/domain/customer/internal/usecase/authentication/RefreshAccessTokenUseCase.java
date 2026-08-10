package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import br.com.api.satireapi.domain.customer.AccessTokenIssuer;
import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerFinder;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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

        var now = Instant.now();
        var currentHash = tokenGenerator.hash(request.refreshToken());
        var customerId = refreshTokenStore.findCustomerIdByHash(currentHash, now)
            .orElseThrow(InvalidRefreshTokenException::new);
        var customer = customerFinder.findById(customerId)
            .filter(Customer::isActive)
            .orElseThrow(InvalidRefreshTokenException::new);

        var rawRefreshToken = tokenGenerator.generate();
        var nextExpiresAt = now.plusSeconds(refreshExpirationSeconds);
        if (!refreshTokenStore.rotateIfCurrent(
            customer.getId(), currentHash, now, tokenGenerator.hash(rawRefreshToken), nextExpiresAt
        )) {
            throw new InvalidRefreshTokenException();
        }

        var profiles = customer.getProfiles().stream().map(Profile::getName).toList();
        var accessToken = accessTokenIssuer.generate(
            customer.getId().toString(), customer.getEmail(), profiles
        );
        return new LoginResponse(
            accessToken,
            "Bearer",
            accessTokenIssuer.expirationSeconds(),
            rawRefreshToken,
            refreshExpirationSeconds
        );
    }
}
