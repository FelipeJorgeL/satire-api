package br.com.api.satireapi.domain.payment.internal.usecase.query;

import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.mapper.PaymentMapper;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderGateway orderGateway;

    public GetPaymentUseCase(
        PaymentRepository paymentRepository,
        PaymentOrderGateway orderGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.orderGateway = orderGateway;
    }

    @Transactional(readOnly = true)
    public PaymentResponse execute(UUID customerId, UUID paymentId) {
        var payment = paymentRepository.findById(paymentId)
            .orElseThrow(PaymentNotFoundException::new);
        if (!orderGateway.isOwnedBy(customerId, payment.getOrderId())) {
            throw new PaymentNotFoundException();
        }
        return PaymentMapper.toResponse(payment);
    }
}
