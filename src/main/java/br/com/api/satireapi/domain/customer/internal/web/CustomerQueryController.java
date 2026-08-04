package br.com.api.satireapi.domain.customer.internal.web;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.api.satireapi.domain.customer.CustomerGateway;
import br.com.api.satireapi.domain.customer.dto.CustomerSummary;

@RestController
@RequestMapping("/api/v1")
class CustomerQueryController {

    private final CustomerGateway customerGateway;

    CustomerQueryController(CustomerGateway customerGateway) {
        this.customerGateway = customerGateway;
    }

    @GetMapping("/me")
    ResponseEntity<CustomerSummary> me(@AuthenticationPrincipal UUID customerId) {
        return customerGateway.findById(customerId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/customers/{id}")
    ResponseEntity<CustomerSummary> findById(@PathVariable UUID id) {
        return customerGateway.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
