package br.com.api.satireapi.domain.inventory.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super("Estoque insuficiente para a saída solicitada");
    }
}
