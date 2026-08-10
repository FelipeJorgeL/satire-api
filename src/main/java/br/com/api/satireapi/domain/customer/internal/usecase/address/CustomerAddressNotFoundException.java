package br.com.api.satireapi.domain.customer.internal.usecase.address;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerAddressNotFoundException extends RuntimeException {

    public CustomerAddressNotFoundException() {
        super("Endereço não encontrado");
    }
}
