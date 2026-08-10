package br.com.api.satireapi.domain.inventory.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockVariationNotFoundException extends RuntimeException {

    public StockVariationNotFoundException() {
        super("Variação do produto não encontrada");
    }
}
