package br.com.api.satireapi.domain.order.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderShipmentNotFoundException extends RuntimeException {

    public OrderShipmentNotFoundException() {
        super("Entrega não encontrada");
    }
}
