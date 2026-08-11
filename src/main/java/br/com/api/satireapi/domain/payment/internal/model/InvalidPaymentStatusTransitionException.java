package br.com.api.satireapi.domain.payment.internal.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidPaymentStatusTransitionException extends RuntimeException {

    public InvalidPaymentStatusTransitionException() {
        super("A atualização do pagamento não é permitida no estado atual");
    }
}
