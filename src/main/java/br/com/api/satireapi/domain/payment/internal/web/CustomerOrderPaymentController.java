package br.com.api.satireapi.domain.payment.internal.web;

import br.com.api.satireapi.domain.payment.internal.dto.request.CreatePaymentRequest;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.usecase.creation.CreatePaymentUseCase;
import br.com.api.satireapi.domain.payment.internal.usecase.query.ListOrderPaymentsUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/orders/{orderId}/payments")
class CustomerOrderPaymentController {

    private final CreatePaymentUseCase createPaymentUseCase;
    private final ListOrderPaymentsUseCase listOrderPaymentsUseCase;

    CustomerOrderPaymentController(
        CreatePaymentUseCase createPaymentUseCase,
        ListOrderPaymentsUseCase listOrderPaymentsUseCase
    ) {
        this.createPaymentUseCase = createPaymentUseCase;
        this.listOrderPaymentsUseCase = listOrderPaymentsUseCase;
    }

    @PostMapping
    ResponseEntity<PaymentResponse> create(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId,
        @RequestHeader("Idempotency-Key")
        @Pattern(regexp = "[A-Za-z0-9._:-]{16,255}") String idempotencyKey,
        @Valid @RequestBody CreatePaymentRequest request
    ) {
        var result = createPaymentUseCase.execute(
            customerId, orderId, idempotencyKey, request
        );
        var builder = result.created()
            ? ResponseEntity.created(URI.create("/api/v1/payments/" + result.payment().id()))
            : ResponseEntity.ok();
        return builder.body(result.payment());
    }

    @GetMapping
    List<PaymentResponse> list(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId
    ) {
        return listOrderPaymentsUseCase.execute(customerId, orderId);
    }
}
