package br.com.api.satireapi.domain.customer.internal.web;

import br.com.api.satireapi.domain.customer.internal.dto.request.LoginRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerResponse;
import br.com.api.satireapi.domain.customer.internal.dto.response.LoginResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.AuthenticateCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
class CustomerController {

    private final RegisterCustomerUseCase registerCustomer;
    private final AuthenticateCustomerUseCase authenticateCustomer;

    CustomerController(RegisterCustomerUseCase registerCustomer, AuthenticateCustomerUseCase authenticateCustomer) {
        this.registerCustomer = registerCustomer;
        this.authenticateCustomer = authenticateCustomer;
    }

    @PostMapping("/register")
    ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterCustomerRequest request) {
        var response = registerCustomer.execute(request);
        return ResponseEntity.created(URI.create("/api/v1/me")).body(response);
    }

    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticateCustomer.execute(request));
    }
}
