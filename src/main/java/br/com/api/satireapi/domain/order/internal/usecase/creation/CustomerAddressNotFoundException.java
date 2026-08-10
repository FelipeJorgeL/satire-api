package br.com.api.satireapi.domain.order.internal.usecase.creation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerAddressNotFoundException extends RuntimeException {

    public CustomerAddressNotFoundException() {
        super("Customer address not found");
    }
}
