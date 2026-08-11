package br.com.api.satireapi.domain.payment.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PaymentOrderNotFoundException extends RuntimeException {

    public PaymentOrderNotFoundException() {
        super("Pedido não encontrado");
    }
}
