package br.com.api.satireapi.domain.cart.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CartQuantityExceededException extends RuntimeException {

    public CartQuantityExceededException() {
        super("Cart quantity cannot exceed 99");
    }
}
