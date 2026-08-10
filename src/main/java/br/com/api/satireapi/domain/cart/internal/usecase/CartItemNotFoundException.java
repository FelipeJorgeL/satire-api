package br.com.api.satireapi.domain.cart.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException() {
        super("Cart item not found");
    }
}
