package br.com.api.satireapi.domain.order.internal.usecase.creation;

import br.com.api.satireapi.domain.cart.CartCheckoutGateway;
import br.com.api.satireapi.domain.cart.CartCheckoutSnapshot;
import br.com.api.satireapi.domain.customer.CustomerAddress;
import br.com.api.satireapi.domain.customer.CustomerAddressGateway;
import br.com.api.satireapi.domain.inventory.StockSaleGateway;
import br.com.api.satireapi.domain.inventory.StockSaleLine;
import br.com.api.satireapi.domain.order.internal.dto.request.CreateOrderRequest;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderResponse;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.model.OrderAddressSnapshot;
import br.com.api.satireapi.domain.order.internal.model.OrderItem;
import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import br.com.api.satireapi.domain.order.internal.persistence.OrderAddressSnapshotRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderItemRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderStatusHistoryRepository;
import br.com.api.satireapi.domain.shipping.ShipmentCreationGateway;
import br.com.api.satireapi.domain.shipping.ShippingQuoteGateway;
import br.com.api.satireapi.domain.shipping.ShippingQuoteRequest;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateOrderUseCase {

    private final CartCheckoutGateway cartCheckoutGateway;
    private final CustomerAddressGateway customerAddressGateway;
    private final ShippingQuoteGateway shippingQuoteGateway;
    private final StockSaleGateway stockSaleGateway;
    private final ShipmentCreationGateway shipmentCreationGateway;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final OrderAddressSnapshotRepository addressRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    public CreateOrderUseCase(
        CartCheckoutGateway cartCheckoutGateway,
        CustomerAddressGateway customerAddressGateway,
        ShippingQuoteGateway shippingQuoteGateway,
        StockSaleGateway stockSaleGateway,
        ShipmentCreationGateway shipmentCreationGateway,
        OrderRepository orderRepository,
        OrderItemRepository itemRepository,
        OrderAddressSnapshotRepository addressRepository,
        OrderStatusHistoryRepository historyRepository,
        OrderNumberGenerator orderNumberGenerator
    ) {
        this.cartCheckoutGateway = cartCheckoutGateway;
        this.customerAddressGateway = customerAddressGateway;
        this.shippingQuoteGateway = shippingQuoteGateway;
        this.stockSaleGateway = stockSaleGateway;
        this.shipmentCreationGateway = shipmentCreationGateway;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.addressRepository = addressRepository;
        this.historyRepository = historyRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    @Transactional
    public CustomerOrderResponse execute(UUID customerId, CreateOrderRequest request) {
        var address = customerAddressGateway.findOwnedById(customerId, request.addressId())
            .orElseThrow(CustomerAddressNotFoundException::new);
        var cart = cartCheckoutGateway.loadForCheckout(customerId);
        if (cart.items().isEmpty()) {
            throw new CheckoutCartEmptyException();
        }
        validateItems(cart);
        var subtotal = subtotal(cart);
        var quote = shippingQuoteGateway.quote(new ShippingQuoteRequest(
            address.postalCode(), subtotal, cart.items().size()
        ));
        var order = Order.place(
            customerId, orderNumberGenerator.generate(), subtotal, quote.fee()
        );
        orderRepository.saveAndFlush(order);
        stockSaleGateway.registerSale(order.getId(), customerId, cart.items().stream()
            .map(item -> new StockSaleLine(item.variationId(), item.quantity()))
            .toList());
        itemRepository.saveAll(cart.items().stream()
            .map(item -> OrderItem.create(
                order.getId(), item.variationId(), item.sku(), item.productName(),
                item.variationName(), item.unitPrice(), item.quantity()
            ))
            .toList());
        addressRepository.save(toAddressSnapshot(order.getId(), address));
        historyRepository.save(OrderStatusHistory.initial(order.getId(), customerId));
        shipmentCreationGateway.ensureForOrder(order.getId(), quote.estimatedDelivery());
        cartCheckoutGateway.clear(customerId);
        return toResponse(order);
    }

    private static void validateItems(CartCheckoutSnapshot cart) {
        if (cart.items().stream().anyMatch(item ->
            !item.active() || item.quantity() <= 0 || item.stock() < item.quantity())) {
            throw new CheckoutItemUnavailableException();
        }
    }

    private static BigDecimal subtotal(CartCheckoutSnapshot cart) {
        return cart.items().stream()
            .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static OrderAddressSnapshot toAddressSnapshot(UUID orderId, CustomerAddress address) {
        return OrderAddressSnapshot.create(
            orderId, address.recipient(), address.postalCode(), address.street(),
            address.number(), address.complement(), address.neighborhood(),
            address.city(), address.state()
        );
    }

    private static CustomerOrderResponse toResponse(Order order) {
        return new CustomerOrderResponse(
            order.getId(), order.getNumber(), order.getStatus(), order.getSubtotal(),
            order.getDiscount(), order.getShippingFee(), order.getTotal(), order.getCreatedAt()
        );
    }
}
