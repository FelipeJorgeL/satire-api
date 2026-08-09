package br.com.api.satireapi.domain.customer.internal.web;

import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.AuthenticateCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmEmailUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerEmailAlreadyExistsException;
import br.com.api.satireapi.domain.customer.internal.usecase.LogoutUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.RefreshAccessTokenUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class CustomerController {

    private final RegisterCustomerUseCase registerCustomer;
    private final AuthenticateCustomerUseCase authenticateCustomer;
    private final RefreshAccessTokenUseCase refreshAccessToken;
    private final LogoutUseCase logout;
    private final ConfirmEmailUseCase confirmEmail;

    CustomerController(
        RegisterCustomerUseCase registerCustomer,
        AuthenticateCustomerUseCase authenticateCustomer,
        RefreshAccessTokenUseCase refreshAccessToken,
        LogoutUseCase logout,
        ConfirmEmailUseCase confirmEmail
    ) {
        this.registerCustomer = registerCustomer;
        this.authenticateCustomer = authenticateCustomer;
        this.refreshAccessToken = refreshAccessToken;
        this.logout = logout;
        this.confirmEmail = confirmEmail;
    }

    @PostMapping("/register")
    ResponseEntity<Void> register(@Valid @RequestBody RegisterCustomerRequest request) {
        try {
            registerCustomer.execute(request);
        } catch (CustomerEmailAlreadyExistsException | DataIntegrityViolationException ex) {
            // Resposta idêntica à de sucesso: não revela se o e-mail (ou CPF) já está cadastrado.
            // DataIntegrityViolationException cobre a corrida de dois registros simultâneos.
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticateCustomer.execute(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshAccessToken.execute(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@AuthenticationPrincipal UUID customerId) {
        logout.execute(customerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/confirm")
    ResponseEntity<String> confirm(@RequestParam String token) {
        confirmEmail.execute(token);
        return ResponseEntity.ok("E-mail confirmado. Você já pode fazer login.");
    }
}
