package br.com.api.satireapi.domain.payment.internal.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidPaymentWebhookSignatureException extends RuntimeException {

    public InvalidPaymentWebhookSignatureException() {
        super("Webhook de pagamento não autenticado");
    }
}
