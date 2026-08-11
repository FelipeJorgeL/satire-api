package br.com.api.satireapi.domain.order.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.inventory.StockReservationGateway;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.request.CancelCustomerOrderRequest;
import br.com.api.satireapi.domain.order.internal.dto.request.CustomerOrderFilter;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.persistence.OrderAddressSnapshotRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderItemRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListCustomerOrdersUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.status.CancelCustomerOrderUseCase;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

class CustomerOrderUseCaseTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository itemRepository = mock(OrderItemRepository.class);
    private final OrderAddressSnapshotRepository addressRepository =
        mock(OrderAddressSnapshotRepository.class);
    private final OrderStatusHistoryRepository historyRepository =
        mock(OrderStatusHistoryRepository.class);
    private final StockReservationGateway stockReservationGateway =
        mock(StockReservationGateway.class);

    @Test
    void listsOnlyOrdersBelongingToAuthenticatedCustomer() {
        var customerId = UUID.randomUUID();
        var pageable = PageRequest.of(0, 20);
        var order = mock(Order.class);
        when(order.getId()).thenReturn(UUID.randomUUID());
        when(order.getNumber()).thenReturn("SAT-123");
        when(order.getStatus()).thenReturn(OrderStatus.AGUARDANDO_PAGAMENTO);
        when(orderRepository.findAll(
            org.mockito.ArgumentMatchers.<Specification<Order>>any(), eq(pageable)
        )).thenReturn(new PageImpl<>(java.util.List.of(order), pageable, 1));

        var result = new ListCustomerOrdersUseCase(orderRepository).execute(
            customerId, new CustomerOrderFilter(null, null, null), pageable
        );

        verify(orderRepository).findAll(
            org.mockito.ArgumentMatchers.<Specification<Order>>any(), eq(pageable)
        );
        org.junit.jupiter.api.Assertions.assertEquals(1, result.getTotalElements());
    }

    @Test
    void doesNotExposeOrderOwnedByAnotherCustomer() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndCustomerId(orderId, customerId))
            .thenReturn(Optional.empty());

        var useCase = new GetCustomerOrderUseCase(
            orderRepository, itemRepository, addressRepository
        );

        assertThrows(OrderNotFoundException.class, () -> useCase.execute(customerId, orderId));
        verify(itemRepository, never()).findAllByOrderIdOrderByCreatedAtAsc(orderId);
    }

    @Test
    void cancelsAwaitingPaymentOrderAndReleasesReservationAtomically() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.AGUARDANDO_PAGAMENTO);
        when(orderRepository.findByIdAndCustomerIdForUpdate(orderId, customerId))
            .thenReturn(Optional.of(order));

        var useCase = new CancelCustomerOrderUseCase(
            orderRepository, historyRepository, stockReservationGateway
        );

        useCase.execute(
            customerId, orderId, new CancelCustomerOrderRequest("Customer changed mind")
        );

        verify(order).changeStatus(OrderStatus.CANCELADO);
        verify(stockReservationGateway).release(orderId);
        verify(historyRepository).save(any());
    }

    @Test
    void refusesCancellationAfterPaymentHasStarted() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var order = mock(Order.class);
        when(order.getStatus()).thenReturn(OrderStatus.PAGO);
        when(orderRepository.findByIdAndCustomerIdForUpdate(orderId, customerId))
            .thenReturn(Optional.of(order));

        var useCase = new CancelCustomerOrderUseCase(
            orderRepository, historyRepository, stockReservationGateway
        );

        assertThrows(
            CustomerOrderCancellationNotAllowedException.class,
            () -> useCase.execute(
                customerId, orderId, new CancelCustomerOrderRequest("Too late")
            )
        );
        verify(order, never()).changeStatus(OrderStatus.CANCELADO);
        verify(stockReservationGateway, never()).release(orderId);
        verify(historyRepository, never()).save(any());
    }

    @Test
    void doesNotReleaseReservationWhenCancellationOrderIsNotOwnedByCustomer() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        when(orderRepository.findByIdAndCustomerIdForUpdate(orderId, customerId))
            .thenReturn(Optional.empty());

        var useCase = new CancelCustomerOrderUseCase(
            orderRepository, historyRepository, stockReservationGateway
        );

        assertThrows(
            OrderNotFoundException.class,
            () -> useCase.execute(
                customerId, orderId, new CancelCustomerOrderRequest("Not my order")
            )
        );
        verify(stockReservationGateway, never()).release(orderId);
        verify(historyRepository, never()).save(any());
    }
}
