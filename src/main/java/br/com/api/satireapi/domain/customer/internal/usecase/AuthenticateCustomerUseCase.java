package br.com.api.satireapi.domain.customer.internal.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final String timingEqualizerHash;

    public AuthenticateCustomerUseCase(
        CustomerFinder customerFinder,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.customerFinder = customerFinder;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        // Hash sacrificial: paga o custo de BCrypt mesmo quando o e-mail não existe,
        // impedindo enumeração de contas pela diferença de tempo de resposta.
        this.timingEqualizerHash = passwordEncoder.encode(TIMING_EQUALIZER_PASSWORD);
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        var normalizedEmail = Customer.normalizeEmail(request.email());
        var customer = customerFinder.findByEmail(normalizedEmail).filter(Customer::isActive);
        var passwordHash = customer.map(Customer::getPasswordHash).orElse(timingEqualizerHash);
        var passwordMatches = passwordEncoder.matches(request.password(), passwordHash);

        var authenticated = customer
            .filter(found -> passwordMatches)
            .orElseThrow(InvalidCredentialsException::new);

        var profiles = authenticated.getProfiles().stream().map(Profile::getName).toList();
        var token = jwtTokenService.generate(authenticated.getId().toString(), authenticated.getEmail(), profiles);

        return new LoginResponse(token, "Bearer", jwtTokenService.expirationSeconds());
    }
}
