package br.com.api.satireapi.domain.payment.internal.web;

import br.com.api.satireapi.domain.payment.internal.usecase.webhook.ProcessPaymentWebhookUseCase;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/payments")
class PaymentWebhookController {

    private static final int MAX_PAYLOAD_LENGTH = 8_192;

    private final ProcessPaymentWebhookUseCase webhookUseCase;

    PaymentWebhookController(ProcessPaymentWebhookUseCase webhookUseCase) {
        this.webhookUseCase = webhookUseCase;
    }

    @PostMapping("/{paymentId}/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void receive(
        @PathVariable UUID paymentId,
        @RequestHeader("X-Webhook-Event-Id")
        @Pattern(regexp = "[A-Za-z0-9._:-]{8,100}") String eventId,
        @RequestHeader("X-Webhook-Timestamp") @Size(max = 64) String timestamp,
        @RequestHeader("X-Webhook-Signature") @Size(max = 256) String signature,
        @RequestBody @Size(max = MAX_PAYLOAD_LENGTH) String rawPayload
    ) {
        webhookUseCase.execute(paymentId, eventId, timestamp, signature, rawPayload);
    }
}
