package br.com.api.satireapi.domain.customer.internal.usecase.administration;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LastAdminRemovalNotAllowedException extends RuntimeException {

    public LastAdminRemovalNotAllowedException() {
        super("Não é permitido remover o último administrador");
    }
}
