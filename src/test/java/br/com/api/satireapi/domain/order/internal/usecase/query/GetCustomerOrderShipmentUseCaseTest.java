package br.com.api.satireapi.domain.order.internal.usecase.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderShipmentNotFoundException;
import br.com.api.satireapi.domain.shipping.CustomerShipment;
import br.com.api.satireapi.domain.shipping.CustomerShipmentGateway;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetCustomerOrderShipmentUseCaseTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CustomerShipmentGateway shipmentGateway = mock(CustomerShipmentGateway.class);
    private final GetCustomerOrderShipmentUseCase useCase =
        new GetCustomerOrderShipmentUseCase(orderRepository, shipmentGateway);

    @Test
    void returnsShipmentOnlyForOwnedOrder() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var shipment = shipment(orderId);
        when(orderRepository.existsByIdAndCustomerId(orderId, customerId)).thenReturn(true);
        when(shipmentGateway.findByOrderId(orderId)).thenReturn(Optional.of(shipment));

        var response = useCase.execute(customerId, orderId);

        assertEquals(shipment, response);
    }

    @Test
    void hidesShipmentWhenOrderIsNotOwnedByCustomer() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(orderRepository.existsByIdAndCustomerId(orderId, customerId)).thenReturn(false);

        assertThrows(
            OrderShipmentNotFoundException.class,
            () -> useCase.execute(customerId, orderId)
        );
        verify(shipmentGateway, never()).findByOrderId(orderId);
    }

    @Test
    void rejectsOwnedOrderWithoutShipment() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(orderRepository.existsByIdAndCustomerId(orderId, customerId)).thenReturn(true);
        when(shipmentGateway.findByOrderId(orderId)).thenReturn(Optional.empty());

        assertThrows(
            OrderShipmentNotFoundException.class,
            () -> useCase.execute(customerId, orderId)
        );
    }

    private CustomerShipment shipment(UUID orderId) {
        return new CustomerShipment(
            UUID.randomUUID(), orderId, "Correios", "BR123456789", "AGUARDANDO_ENVIO",
            null, null, LocalDate.now().plusDays(5)
        );
    }
}
