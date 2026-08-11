package br.com.api.satireapi.domain.order.internal.persistence;

import br.com.api.satireapi.domain.order.OrderPaymentNotAllowedException;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.PaymentOrder;
import br.com.api.satireapi.domain.order.PaymentOrderGateway;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaPaymentOrderGateway implements PaymentOrderGateway {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    JpaPaymentOrderGateway(
        OrderRepository orderRepository,
        OrderStatusHistoryRepository historyRepository
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    @Override
    public Optional<PaymentOrder> findOwnedForUpdate(UUID customerId, UUID orderId) {
        return orderRepository.findByIdAndCustomerIdForUpdate(orderId, customerId)
            .map(this::toPaymentOrder);
    }

    @Override
    public boolean isOwnedBy(UUID customerId, UUID orderId) {
        return orderRepository.existsByIdAndCustomerId(orderId, customerId);
    }

    @Override
    public void markPaid(UUID orderId) {
        var order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(OrderPaymentNotAllowedException::new);
        if (order.getStatus() == OrderStatus.PAGO) {
            return;
        }
        if (order.getStatus() != OrderStatus.AGUARDANDO_PAGAMENTO) {
            throw new OrderPaymentNotAllowedException();
        }
        var previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.PAGO);
        historyRepository.save(OrderStatusHistory.recordSystem(
            orderId,
            previousStatus,
            OrderStatus.PAGO,
            "Pagamento simulado aprovado"
        ));
    }

    private PaymentOrder toPaymentOrder(Order order) {
        return new PaymentOrder(
            order.getId(), order.getCustomerId(), order.getStatus(), order.getTotal()
        );
    }
}
