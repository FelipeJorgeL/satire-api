package br.com.api.satireapi.domain.payment.internal.usecase.query;

import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.payment.internal.dto.response.PaymentResponse;
import br.com.api.satireapi.domain.payment.internal.mapper.PaymentMapper;
import br.com.api.satireapi.domain.payment.internal.persistence.PaymentRepository;
import br.com.api.satireapi.domain.payment.internal.usecase.PaymentOrderNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListOrderPaymentsUseCase {

    private final PaymentRepository paymentRepository;
    private final PaymentOrderGateway orderGateway;

    public ListOrderPaymentsUseCase(
        PaymentRepository paymentRepository,
        PaymentOrderGateway orderGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.orderGateway = orderGateway;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> execute(UUID customerId, UUID orderId) {
        if (!orderGateway.isOwnedBy(customerId, orderId)) {
            throw new PaymentOrderNotFoundException();
        }
        return paymentRepository.findAllByOrderIdOrderByCreatedAtDesc(orderId).stream()
            .map(PaymentMapper::toResponse)
            .toList();
    }
}
