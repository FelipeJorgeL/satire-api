package br.com.api.satireapi.domain.order.internal.usecase;

import br.com.api.satireapi.domain.order.OrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CustomerOrderCancellationNotAllowedException extends RuntimeException {

    public CustomerOrderCancellationNotAllowedException(OrderStatus status) {
        super("O pedido não pode ser cancelado no status " + status);
    }
}
