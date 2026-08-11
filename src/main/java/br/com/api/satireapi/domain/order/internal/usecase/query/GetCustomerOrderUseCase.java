package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.mapper.OrderMapper;
import br.com.api.satireapi.domain.order.internal.persistence.OrderAddressSnapshotRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderItemRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCustomerOrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final OrderAddressSnapshotRepository addressRepository;

    public GetCustomerOrderUseCase(
        OrderRepository orderRepository,
        OrderItemRepository itemRepository,
        OrderAddressSnapshotRepository addressRepository
    ) {
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional(readOnly = true)
    public CustomerOrderDetailsResponse execute(UUID customerId, UUID orderId) {
        var order = orderRepository.findByIdAndCustomerId(orderId, customerId)
            .orElseThrow(OrderNotFoundException::new);
        return OrderMapper.toCustomerDetails(
            order,
            itemRepository.findAllByOrderIdOrderByCreatedAtAsc(orderId),
            addressRepository.findByOrderId(orderId).orElse(null)
        );
    }
}
