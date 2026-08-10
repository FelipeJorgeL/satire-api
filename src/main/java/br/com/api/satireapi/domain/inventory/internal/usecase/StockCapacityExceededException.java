package br.com.api.satireapi.domain.inventory.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockCapacityExceededException extends RuntimeException {

    public StockCapacityExceededException() {
        super("O novo estoque excede a capacidade suportada");
    }
}
