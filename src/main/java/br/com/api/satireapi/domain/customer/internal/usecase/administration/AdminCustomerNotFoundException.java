package br.com.api.satireapi.domain.customer.internal.usecase.administration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AdminCustomerNotFoundException extends RuntimeException {

    public AdminCustomerNotFoundException() {
        super("Usuário não encontrado");
    }
}
