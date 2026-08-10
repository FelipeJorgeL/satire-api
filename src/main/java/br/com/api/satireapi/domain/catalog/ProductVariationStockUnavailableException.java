package br.com.api.satireapi.domain.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductVariationStockUnavailableException extends RuntimeException {

    public ProductVariationStockUnavailableException() {
        super("Estoque insuficiente para a variação do produto");
    }
}
