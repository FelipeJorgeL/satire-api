package br.com.api.satireapi.domain.customer.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmailConfirmationTokenException extends RuntimeException {

    public InvalidEmailConfirmationTokenException() {
        super("Invalid or expired email confirmation token");
    }
}
