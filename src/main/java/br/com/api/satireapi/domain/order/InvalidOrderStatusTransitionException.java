package br.com.api.satireapi.domain.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidOrderStatusTransitionException extends RuntimeException {

    public InvalidOrderStatusTransitionException(OrderStatus current, OrderStatus target) {
        super("Transição de status inválida: " + current + " para " + target);
    }
}
