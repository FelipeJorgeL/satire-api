package br.com.api.satireapi.domain.payment.internal.usecase;

import br.com.api.satireapi.domain.payment.internal.model.PaymentStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PaymentNotRefundableException extends RuntimeException {

    public PaymentNotRefundableException(PaymentStatus status) {
        super("Pagamento não pode ser estornado no status " + status);
    }
}
