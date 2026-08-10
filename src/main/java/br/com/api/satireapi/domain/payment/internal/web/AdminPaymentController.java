package br.com.api.satireapi.domain.payment.internal.web;

import br.com.api.satireapi.domain.payment.internal.dto.request.RequestPaymentRefund;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentRefundResponse;
import br.com.api.satireapi.domain.payment.internal.usecase.refund.RequestPaymentRefundUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/admin/payments")
class AdminPaymentController {

    private final RequestPaymentRefundUseCase requestPaymentRefundUseCase;

    AdminPaymentController(RequestPaymentRefundUseCase requestPaymentRefundUseCase) {
        this.requestPaymentRefundUseCase = requestPaymentRefundUseCase;
    }

    @PostMapping("/{paymentId}/refund")
    @ResponseStatus(HttpStatus.ACCEPTED)
    PaymentRefundResponse requestRefund(
        @AuthenticationPrincipal UUID adminId,
        @PathVariable UUID paymentId,
        @RequestHeader("Idempotency-Key")
        @Pattern(regexp = "[A-Za-z0-9._:-]{16,255}") String idempotencyKey,
        @Valid @RequestBody RequestPaymentRefund request
    ) {
        return requestPaymentRefundUseCase.execute(adminId, paymentId, idempotencyKey, request);
    }
}
