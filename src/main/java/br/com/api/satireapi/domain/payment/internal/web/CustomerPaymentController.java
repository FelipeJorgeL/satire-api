package br.com.api.satireapi.domain.payment.internal.web;

import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.usecase.query.GetPaymentUseCase;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
class CustomerPaymentController {

    private final GetPaymentUseCase getPaymentUseCase;

    CustomerPaymentController(GetPaymentUseCase getPaymentUseCase) {
        this.getPaymentUseCase = getPaymentUseCase;
    }

    @GetMapping("/{paymentId}")
    PaymentResponse get(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID paymentId
    ) {
        return getPaymentUseCase.execute(customerId, paymentId);
    }
}
