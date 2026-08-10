package br.com.api.satireapi.domain.shipping.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class TrackingCodeAlreadyExistsException extends RuntimeException {

    public TrackingCodeAlreadyExistsException() {
        super("O cÃ³digo de rastreio jÃ¡ estÃ¡ associado a outra entrega");
    }
}
