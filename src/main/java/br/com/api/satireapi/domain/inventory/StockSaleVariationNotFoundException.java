package br.com.api.satireapi.domain.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StockSaleVariationNotFoundException extends RuntimeException {

    public StockSaleVariationNotFoundException() {
        super("Product variation not found");
    }
}
