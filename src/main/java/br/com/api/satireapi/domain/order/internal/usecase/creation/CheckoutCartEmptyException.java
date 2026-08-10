package br.com.api.satireapi.domain.order.internal.usecase.creation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckoutCartEmptyException extends RuntimeException {

    public CheckoutCartEmptyException() {
        super("Cannot create an order from an empty cart");
    }
}
