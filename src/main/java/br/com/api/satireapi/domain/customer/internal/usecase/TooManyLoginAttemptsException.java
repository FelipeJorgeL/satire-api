package br.com.api.satireapi.domain.customer.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyLoginAttemptsException extends RuntimeException {

    public TooManyLoginAttemptsException() {
        super("Too many login attempts; try again later");
    }
}
