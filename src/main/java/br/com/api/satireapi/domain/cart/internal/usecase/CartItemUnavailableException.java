package br.com.api.satireapi.domain.cart.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CartItemUnavailableException extends RuntimeException {

    public CartItemUnavailableException() {
        super("Cart item is no longer available");
    }
}
