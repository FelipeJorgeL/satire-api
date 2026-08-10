package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.infra.security.jwt.JwtTokenService;

@Service
public class AuthenticateCustomerUseCase {

    private static final String TIMING_EQUALIZER_PASSWORD = "timing-equalizer";

    private final CustomerFinder customerFinder;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final LoginAttemptTracker loginAttemptTracker;
    private final RefreshTokenStore refreshTokenStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final long refreshExpirationSeconds;
    private final String timingEqualizerHash;

    public AuthenticateCustomerUseCase(
        CustomerFinder customerFinder,
        PasswordHasher passwordHasher,
        AccessTokenIssuer accessTokenIssuer,
        LoginAttemptTracker loginAttemptTracker,
        RefreshTokenStore refreshTokenStore,
        OpaqueTokenGenerator tokenGenerator,
        @Value("${app.jwt.refresh-expiration}") long refreshExpirationSeconds
    ) {
        this.customerFinder = customerFinder;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.loginAttemptTracker = loginAttemptTracker;
        this.refreshTokenStore = refreshTokenStore;
        this.tokenGenerator = tokenGenerator;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        // Hash sacrificial: paga o custo de BCrypt mesmo quando o e-mail não existe,
        // impedindo enumeração de contas pela diferença de tempo de resposta.
        this.timingEqualizerHash = passwordHasher.encode(TIMING_EQUALIZER_PASSWORD);
    }

    @Transactional
    public LoginResponse execute(LoginRequest request) {
        var normalizedEmail = Customer.normalizeEmail(request.email());
        if (loginAttemptTracker.isBlocked(normalizedEmail)) {
            throw new TooManyLoginAttemptsException();
        }

        var customer = customerFinder.findByEmail(normalizedEmail).filter(Customer::isActive);
        var passwordHash = customer.map(Customer::getPasswordHash).orElse(timingEqualizerHash);
        var passwordMatches = passwordHasher.matches(request.password(), passwordHash);

        if (customer.isEmpty() || !passwordMatches) {
            loginAttemptTracker.recordFailure(normalizedEmail);
            throw new InvalidCredentialsException();
        }

        var authenticated = customer.get();
        loginAttemptTracker.recordSuccess(normalizedEmail);

        var profiles = authenticated.getProfiles().stream().map(Profile::getName).toList();
        var accessToken = accessTokenIssuer.generate(authenticated.getId().toString(), authenticated.getEmail(), profiles);

        var rawRefreshToken = tokenGenerator.generate();
        var refreshExpiresAt = Instant.now().plusSeconds(refreshExpirationSeconds);
        refreshTokenStore.save(authenticated.getId(), tokenGenerator.hash(rawRefreshToken), refreshExpiresAt);

        return new LoginResponse(
            accessToken,
            "Bearer",
            accessTokenIssuer.expirationSeconds(),
            rawRefreshToken,
            refreshExpirationSeconds
        );
    }
}
