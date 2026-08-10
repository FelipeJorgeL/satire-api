package br.com.api.satireapi.domain.inventory.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStockMovementPeriodException extends RuntimeException {

    public InvalidStockMovementPeriodException() {
        super("O início do período não pode ser posterior ao fim");
    }
}
