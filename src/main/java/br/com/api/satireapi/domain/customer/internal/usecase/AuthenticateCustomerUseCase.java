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

    private final CustomerFinder customerFinder;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthenticateCustomerUseCase(
        CustomerFinder customerFinder,
        PasswordEncoder passwordEncoder,
        JwtTokenService jwtTokenService
    ) {
        this.customerFinder = customerFinder;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse execute(LoginRequest request) {
        var normalizedEmail = Customer.normalizeEmail(request.email());
        var customer = customerFinder.findByEmail(normalizedEmail)
            .filter(Customer::isActive)
            .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
            .orElseThrow(InvalidCredentialsException::new);

        var profiles = customer.getProfiles().stream().map(Profile::getName).toList();
        var token = jwtTokenService.generate(customer.getId().toString(), customer.getEmail(), profiles);

        return new LoginResponse(token, "Bearer", jwtTokenService.expirationSeconds());
    }
}
