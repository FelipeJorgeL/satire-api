package br.com.api.satireapi.domain.order.internal.mapper;

import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderAddressResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderItemResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderListItemResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderStatusHistoryResponse;
import br.com.api.satireapi.domain.order.internal.model.Order;
import br.com.api.satireapi.domain.order.internal.model.OrderAddressSnapshot;
import br.com.api.satireapi.domain.order.internal.model.OrderItem;
import br.com.api.satireapi.domain.order.internal.model.OrderStatusHistory;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {
    }

    public static AdminOrderListItemResponse toListItem(Order order) {
        return new AdminOrderListItemResponse(
            order.getId(), order.getCustomerId(), order.getNumber(), order.getStatus(),
            order.getTotal(), order.getCreatedAt(), order.getUpdatedAt()
        );
    }

    public static AdminOrderDetailsResponse toDetails(
        Order order,
        List<OrderItem> items,
        OrderAddressSnapshot address,
        List<OrderStatusHistory> history
    ) {
        return new AdminOrderDetailsResponse(
            order.getId(), order.getCustomerId(), order.getNumber(), order.getStatus(),
            order.getSubtotal(), order.getDiscount(), order.getShippingFee(), order.getTotal(),
            order.getCreatedAt(), order.getUpdatedAt(),
            items.stream().map(OrderMapper::toItem).toList(),
            address == null ? null : toAddress(address),
            history.stream().map(OrderMapper::toHistory).toList()
        );
    }

    private static AdminOrderItemResponse toItem(OrderItem item) {
        return new AdminOrderItemResponse(
            item.getId(), item.getVariationId(), item.getSku(), item.getProductName(),
            item.getVariationName(), item.getUnitPrice(), item.getQuantity(), item.getSubtotal()
        );
    }

    private static AdminOrderAddressResponse toAddress(OrderAddressSnapshot address) {
        return new AdminOrderAddressResponse(
            address.getId(), address.getRecipient(), address.getPostalCode(), address.getStreet(),
            address.getNumber(), address.getComplement(), address.getNeighborhood(),
            address.getCity(), address.getState()
        );
    }

    private static AdminOrderStatusHistoryResponse toHistory(OrderStatusHistory history) {
        return new AdminOrderStatusHistoryResponse(
            history.getId(), history.getUserId(), history.getPreviousStatus(),
            history.getNewStatus(), history.getReason(), history.getCreatedAt()
        );
    }
}
