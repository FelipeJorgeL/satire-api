package br.com.api.satireapi.domain.order.internal.usecase.status;

import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.request.CancelCustomerOrderRequest;
import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.usecase.CustomerOrderCancellationNotAllowedException;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CancelCustomerOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final StockReservationGateway stockReservationGateway;

    public CancelCustomerOrderUseCase(
        OrderRepository orderRepository,
        OrderStatusHistoryRepository historyRepository,
        StockReservationGateway stockReservationGateway
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.stockReservationGateway = stockReservationGateway;
    }

    @Transactional
    public void execute(
        UUID customerId,
        UUID orderId,
        CancelCustomerOrderRequest request
    ) {
        var order = orderRepository.findByIdAndCustomerIdForUpdate(orderId, customerId)
            .orElseThrow(OrderNotFoundException::new);
        if (order.getStatus() != OrderStatus.AGUARDANDO_PAGAMENTO) {
            throw new CustomerOrderCancellationNotAllowedException(order.getStatus());
        }

        var previousStatus = order.getStatus();
        order.changeStatus(OrderStatus.CANCELADO);
        stockReservationGateway.release(orderId);
        historyRepository.save(OrderStatusHistory.record(
            orderId, customerId, previousStatus, OrderStatus.CANCELADO, request.reason()
        ));
    }
}
