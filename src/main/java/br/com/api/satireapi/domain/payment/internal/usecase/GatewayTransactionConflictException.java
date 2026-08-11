package br.com.api.satireapi.domain.payment.internal.usecase;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GatewayTransactionConflictException extends RuntimeException {

    public GatewayTransactionConflictException() {
        super("A transação do gateway já está associada a outro pagamento");
    }
}
