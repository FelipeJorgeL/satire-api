package br.com.api.satireapi.domain.payment.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class WebhookReplayConflictException extends RuntimeException {

    public WebhookReplayConflictException() {
        super("O identificador do evento já foi usado com outro conteúdo");
    }
}
