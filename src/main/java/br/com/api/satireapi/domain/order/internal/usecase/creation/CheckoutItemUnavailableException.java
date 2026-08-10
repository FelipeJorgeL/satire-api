package br.com.api.satireapi.domain.order.internal.usecase.creation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CheckoutItemUnavailableException extends RuntimeException {

    public CheckoutItemUnavailableException() {
        super("A cart item is unavailable for checkout");
    }
}
