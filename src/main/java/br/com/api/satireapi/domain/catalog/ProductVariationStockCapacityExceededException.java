package br.com.api.satireapi.domain.catalog;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductVariationStockCapacityExceededException extends RuntimeException {

    public ProductVariationStockCapacityExceededException() {
        super("O novo estoque excede a capacidade suportada");
    }
}
