package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderStatusHistoryResponse;
import br.com.api.satireapi.domain.order.internal.mapper.OrderMapper;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCustomerOrderStatusHistoryUseCase {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public ListCustomerOrderStatusHistoryUseCase(
        OrderRepository orderRepository,
        OrderStatusHistoryRepository historyRepository
    ) {
        this.orderRepository = orderRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrderStatusHistoryResponse> execute(UUID customerId, UUID orderId) {
        orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(OrderNotFoundException::new);
        return historyRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)
            .stream()
            .map(OrderMapper::toCustomerHistory)
            .toList();
    }
}
