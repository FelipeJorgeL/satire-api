package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderShipmentNotFoundException;
import br.com.api.satireapi.domain.shipping.CustomerShipment;
import br.com.api.satireapi.domain.shipping.CustomerShipmentGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCustomerOrderShipmentUseCase {

    private final OrderRepository orderRepository;
    private final CustomerShipmentGateway shipmentGateway;

    public GetCustomerOrderShipmentUseCase(
        OrderRepository orderRepository,
        CustomerShipmentGateway shipmentGateway
    ) {
        this.orderRepository = orderRepository;
        this.shipmentGateway = shipmentGateway;
    }

    @Transactional(readOnly = true)
    public CustomerShipment execute(UUID customerId, UUID orderId) {
        if (!orderRepository.existsByIdAndCustomerId(orderId, customerId)) {
            throw new OrderShipmentNotFoundException();
        }
        return shipmentGateway.findByOrderId(orderId)
            .orElseThrow(OrderShipmentNotFoundException::new);
    }
}
