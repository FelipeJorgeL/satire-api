package br.com.api.satireapi.domain.order.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ReviewPurchaseRequiredException extends RuntimeException {

    public ReviewPurchaseRequiredException() {
        super("A avaliação exige um pedido entregue contendo o produto");
    }
}
