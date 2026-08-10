package br.com.api.satireapi.domain.customer.internal.usecase.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerAccountNotFoundException extends RuntimeException {

    public CustomerAccountNotFoundException() {
        super("Cliente não encontrado");
    }
}
