package br.com.api.satireapi.domain.payment.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPaymentWebhookPayloadException extends RuntimeException {

    public InvalidPaymentWebhookPayloadException() {
        super("Payload do webhook de pagamento é inválido");
    }
}
