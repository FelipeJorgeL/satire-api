package br.com.api.satireapi.domain.order.internal.usecase.status;

import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import br.com.api.satireapi.domain.order.internal.dto.request.UpdateOrderStatusRequest;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeAdminOrderStatusUseCase {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final StockReservationGateway stockReservationGateway;

    public ChangeAdminOrderStatusUseCase(
        OrderRepository orderRepository,
        OrderStatusHistoryRepository historyRepository,
        StockReservationGateway stockReservationGateway
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
        this.stockReservationGateway = stockReservationGateway;
    }

    @Transactional
    public void execute(UUID adminId, UUID orderId, UpdateOrderStatusRequest request) {
        var order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(OrderNotFoundException::new);
        var previousStatus = order.getStatus();
        order.changeStatus(request.status());
        if (request.status() == br.com.api.satireapi.domain.order.OrderStatus.CANCELADO) {
            stockReservationGateway.release(orderId);
        }
        historyRepository.save(OrderStatusHistory.record(
            orderId, adminId, previousStatus, request.status(), request.reason()
        ));
    }
}
