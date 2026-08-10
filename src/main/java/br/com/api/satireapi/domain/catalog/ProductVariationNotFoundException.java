package br.com.api.satireapi.domain.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductVariationNotFoundException extends RuntimeException {

    public ProductVariationNotFoundException() {
        super("Variação do produto não encontrada");
    }
}
