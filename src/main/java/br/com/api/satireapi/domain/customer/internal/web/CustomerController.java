package br.com.api.satireapi.domain.customer.internal.web;

import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RefreshTokenRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.ResendConfirmationRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerEmailAlreadyExistsException;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.AuthenticateCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.ConfirmEmailUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.LogoutUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RefreshAccessTokenUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RegisterCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.RegistrationRateLimiter;
import br.com.api.satireapi.domain.customer.internal.usecase.authentication.ResendConfirmationEmailUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
class CustomerController {

    private final RegisterCustomerUseCase registerCustomer;
    private final AuthenticateCustomerUseCase authenticateCustomer;
    private final RefreshAccessTokenUseCase refreshAccessToken;
    private final LogoutUseCase logout;
    private final ConfirmEmailUseCase confirmEmail;
    private final RegistrationRateLimiter registrationRateLimiter;
    private final ResendConfirmationEmailUseCase resendConfirmationEmail;

    CustomerController(
        RegisterCustomerUseCase registerCustomer,
        AuthenticateCustomerUseCase authenticateCustomer,
        RefreshAccessTokenUseCase refreshAccessToken,
        LogoutUseCase logout,
        ConfirmEmailUseCase confirmEmail,
        RegistrationRateLimiter registrationRateLimiter,
        ResendConfirmationEmailUseCase resendConfirmationEmail
    ) {
        this.registerCustomer = registerCustomer;
        this.authenticateCustomer = authenticateCustomer;
        this.refreshAccessToken = refreshAccessToken;
        this.logout = logout;
        this.confirmEmail = confirmEmail;
        this.registrationRateLimiter = registrationRateLimiter;
        this.resendConfirmationEmail = resendConfirmationEmail;
    }

    @PostMapping("/register")
    ResponseEntity<Void> register(
        @Valid @RequestBody RegisterCustomerRequest request,
        HttpServletRequest httpRequest
    ) {
        if (!registrationRateLimiter.allow(httpRequest.getRemoteAddr(), request.email(), request.cpf())) {
            return ResponseEntity.accepted().build();
        }
        try {
            registerCustomer.execute(request);
        } catch (CustomerEmailAlreadyExistsException | DataIntegrityViolationException exception) {
            // Resposta idêntica à de sucesso: não revela se o e-mail ou CPF já existe.
        }
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/confirm/resend")
    ResponseEntity<Void> resendConfirmation(
        @Valid @RequestBody ResendConfirmationRequest request,
        HttpServletRequest httpRequest
    ) {
        if (registrationRateLimiter.allow(httpRequest.getRemoteAddr(), request.email(), null)) {
            resendConfirmationEmail.execute(request);
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

    @PostMapping("/confirm")
    ResponseEntity<String> confirm(@RequestParam @Size(max = 256) String token) {
        confirmEmail.execute(token);
        return ResponseEntity.ok("E-mail confirmado. Você já pode fazer login.");
    }

    @GetMapping("/confirm")
    ResponseEntity<String> confirmationPage(@RequestParam @Size(max = 256) String token) {
        var escapedToken = escapeHtml(token);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_HTML)
            .body("""
            <!doctype html>
            <html lang="pt-BR">
              <body>
                <p>Confirme seu e-mail para ativar sua conta.</p>
                <form method="post" action="/api/v1/auth/confirm">
                  <input type="hidden" name="token" value="%s">
                  <button type="submit">Confirmar e-mail</button>
                </form>
              </body>
            </html>
            """.formatted(escapedToken));
    }

    private static String escapeHtml(String value) {
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
