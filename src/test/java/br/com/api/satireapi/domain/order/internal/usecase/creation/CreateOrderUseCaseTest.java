package br.com.api.satireapi.domain.order.internal.usecase.creation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.cart.CartCheckoutGateway;
import br.com.api.satireapi.domain.cart.CartCheckoutSnapshot;
import br.com.api.satireapi.domain.cart.CartItemSnapshot;
import br.com.api.satireapi.domain.customer.CustomerAddress;
import br.com.api.satireapi.domain.customer.CustomerAddressGateway;
import br.com.api.satireapi.domain.inventory.StockSaleGateway;
import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.request.CreateOrderRequest;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.persistence.OrderAddressSnapshotRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderItemRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.shipping.ShipmentCreationGateway;
import br.com.api.satireapi.domain.shipping.ShippingQuote;
import br.com.api.satireapi.domain.shipping.ShippingQuoteGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CreateOrderUseCaseTest {

    private final CartCheckoutGateway cartGateway = mock(CartCheckoutGateway.class);
    private final CustomerAddressGateway addressGateway = mock(CustomerAddressGateway.class);
    private final ShippingQuoteGateway quoteGateway = mock(ShippingQuoteGateway.class);
    private final StockSaleGateway stockGateway = mock(StockSaleGateway.class);
    private final ShipmentCreationGateway shipmentGateway = mock(ShipmentCreationGateway.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository itemRepository = mock(OrderItemRepository.class);
    private final OrderAddressSnapshotRepository addressRepository =
        mock(OrderAddressSnapshotRepository.class);
    private final OrderStatusHistoryRepository historyRepository =
        mock(OrderStatusHistoryRepository.class);
    private final OrderNumberGenerator numberGenerator = mock(OrderNumberGenerator.class);
    private final CreateOrderUseCase useCase = new CreateOrderUseCase(
        cartGateway, addressGateway, quoteGateway, stockGateway, shipmentGateway,
        orderRepository, itemRepository, addressRepository, historyRepository, numberGenerator
    );

    @Test
    void rejectsCustomerAddressThatDoesNotBelongToCustomer() {
        var customerId = UUID.randomUUID();
        when(addressGateway.findOwnedById(customerId, UUID.randomUUID()))
            .thenReturn(Optional.empty());

        assertThrows(
            CustomerAddressNotFoundException.class,
            () -> useCase.execute(
                customerId, new CreateOrderRequest(UUID.randomUUID())
            )
        );
        verify(cartGateway, never()).loadForCheckout(customerId);
    }

    @Test
    void rejectsEmptyCart() {
        var customerId = UUID.randomUUID();
        var addressId = UUID.randomUUID();
        when(addressGateway.findOwnedById(customerId, addressId)).thenReturn(Optional.of(address()));
        when(cartGateway.loadForCheckout(customerId))
            .thenReturn(new CartCheckoutSnapshot(null, customerId, List.of()));

        assertThrows(
            CheckoutCartEmptyException.class,
            () -> useCase.execute(customerId, new CreateOrderRequest(addressId))
        );
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void createsOrderSellsStockCreatesShipmentAndClearsCartAtomically() {
        var customerId = UUID.randomUUID();
        var addressId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var address = address();
        var item = new CartItemSnapshot(
            UUID.randomUUID(), variationId, UUID.randomUUID(), "T-Shirt", "SKU-1",
            "Blue / M", new BigDecimal("40.00"), 5, true, 2
        );
        var cart = new CartCheckoutSnapshot(UUID.randomUUID(), customerId, List.of(item));
        var quoteDate = LocalDate.now().plusDays(5);
        when(addressGateway.findOwnedById(customerId, addressId)).thenReturn(Optional.of(address));
        when(cartGateway.loadForCheckout(customerId)).thenReturn(cart);
        when(quoteGateway.quote(any())).thenReturn(
            new ShippingQuote("STANDARD-SIMULATED", new BigDecimal("14.90"), quoteDate)
        );
        when(numberGenerator.generate()).thenReturn("SAT-TEST-1");
        when(orderRepository.saveAndFlush(any(Order.class)))
            .thenAnswer(invocation -> {
                var savedOrder = invocation.<Order>getArgument(0);
                ReflectionTestUtils.setField(savedOrder, "id", UUID.randomUUID());
                return savedOrder;
            });

        var response = useCase.execute(customerId, new CreateOrderRequest(addressId));

        assertEquals(OrderStatus.AGUARDANDO_PAGAMENTO, response.status());
        assertEquals(new BigDecimal("94.90"), response.total());
        verify(stockGateway).registerSale(any(), eq(customerId), any());
        verify(itemRepository).saveAll(any());
        verify(addressRepository).save(any());
        verify(historyRepository).save(any());
        verify(shipmentGateway).ensureForOrder(any(), eq(quoteDate));
        verify(cartGateway).clear(customerId);
    }

    private static CustomerAddress address() {
        return new CustomerAddress(
            UUID.randomUUID(), UUID.randomUUID(), "Customer", "01001000", "Street",
            "10", null, "Center", "Sao Paulo", "SP"
        );
    }
}
