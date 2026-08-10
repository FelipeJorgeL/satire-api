package br.com.api.satireapi.domain.shipping.internal.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidShippingStatusTransitionException extends RuntimeException {

    public InvalidShippingStatusTransitionException(
        ShippingStatus current,
        ShippingStatus target
    ) {
        super("TransiÃ§Ã£o logÃ­stica invÃ¡lida: " + current + " -> " + target);
    }
}
