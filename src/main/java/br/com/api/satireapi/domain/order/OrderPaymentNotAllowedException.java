package br.com.api.satireapi.domain.order;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrderPaymentNotAllowedException extends RuntimeException {

    public OrderPaymentNotAllowedException() {
        super("O pedido não aceita esta operação de pagamento");
    }
}
