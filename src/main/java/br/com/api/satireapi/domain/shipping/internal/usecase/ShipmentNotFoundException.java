package br.com.api.satireapi.domain.shipping.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException() {
        super("Entrega nÃ£o encontrada");
    }
}
