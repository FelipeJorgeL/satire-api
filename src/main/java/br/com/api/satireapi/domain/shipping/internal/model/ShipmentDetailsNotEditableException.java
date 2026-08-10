package br.com.api.satireapi.domain.shipping.internal.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ShipmentDetailsNotEditableException extends RuntimeException {

    public ShipmentDetailsNotEditableException() {
        super("Os dados da entrega nÃ£o podem ser alterados em um estado terminal");
    }
}
