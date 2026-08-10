package br.com.api.satireapi.domain.order.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOrderPeriodException extends RuntimeException {

    public InvalidOrderPeriodException() {
        super("O início do período não pode ser posterior ao fim");
    }
}
