package br.com.api.satireapi.domain.order.internal.usecase.status;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import br.com.api.satireapi.domain.order.internal.dto.request.UpdateOrderStatusRequest;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.usecase.OrderNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChangeAdminOrderStatusUseCaseTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderStatusHistoryRepository historyRepository = mock(OrderStatusHistoryRepository.class);
    private final StockReservationGateway stockReservationGateway = mock(StockReservationGateway.class);
    private final ChangeAdminOrderStatusUseCase useCase = new ChangeAdminOrderStatusUseCase(
        orderRepository, historyRepository, stockReservationGateway
    );

    @Test
    void changesStatusWithPessimisticLookupAndCreatesHistory() {
        var orderId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PAGO);
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        useCase.execute(
            adminId,
            orderId,
            new UpdateOrderStatusRequest(OrderStatus.EM_SEPARACAO, null)
        );

        verify(order).changeStatus(OrderStatus.EM_SEPARACAO);
        verify(historyRepository).save(any());
    }

    @Test
    void returnsNotFoundWhenOrderDoesNotExist() {
        var orderId = UUID.randomUUID();
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.empty());

        assertThrows(
            OrderNotFoundException.class,
            () -> useCase.execute(
                UUID.randomUUID(),
                orderId,
                new UpdateOrderStatusRequest(OrderStatus.PAGO, null)
            )
        );
    }

    @Test
    void releasesReservationWhenOrderIsCancelled() {
        var orderId = UUID.randomUUID();
        var adminId = UUID.randomUUID();
        var order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.AGUARDANDO_PAGAMENTO);
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        useCase.execute(
            adminId,
            orderId,
            new UpdateOrderStatusRequest(OrderStatus.CANCELADO, "Customer requested cancellation")
        );

        verify(order).changeStatus(OrderStatus.CANCELADO);
        verify(stockReservationGateway).release(orderId);
        verify(historyRepository).save(any());
    }
}
