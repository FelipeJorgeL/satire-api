package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.mapper.OrderMapper;
import br.com.api.satireapi.domain.order.internal.persistence.OrderAddressSnapshotRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderItemRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAdminOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final OrderAddressSnapshotRepository addressRepository;
    private final OrderStatusHistoryRepository historyRepository;

    public GetAdminOrderUseCase(
        OrderRepository orderRepository,
        OrderItemRepository itemRepository,
        OrderAddressSnapshotRepository addressRepository,
        OrderStatusHistoryRepository historyRepository
    ) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.addressRepository = addressRepository;
        this.historyRepository = historyRepository;
    }

    @Transactional(readOnly = true)
    public AdminOrderDetailsResponse execute(UUID orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(OrderNotFoundException::new);
        return OrderMapper.toDetails(
            order,
            itemRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId),
            addressRepository.findByOrderId(orderId).orElse(null),
            historyRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId)
        );
    }
}
