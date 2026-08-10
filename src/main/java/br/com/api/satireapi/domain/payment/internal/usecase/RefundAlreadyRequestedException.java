package br.com.api.satireapi.domain.payment.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RefundAlreadyRequestedException extends RuntimeException {

    public RefundAlreadyRequestedException() {
        super("Já existe uma solicitação de estorno para este pagamento");
    }
}
