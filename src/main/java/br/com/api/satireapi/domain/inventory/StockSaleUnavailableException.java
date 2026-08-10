package br.com.api.satireapi.domain.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StockSaleUnavailableException extends RuntimeException {

    public StockSaleUnavailableException() {
        super("A variation is not available for sale");
    }
}
