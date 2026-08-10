package br.com.api.satireapi.domain.order.internal.dto.response;

import br.com.api.satireapi.domain.order.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AdminOrderDetailsResponse(
    UUID id,
    UUID customerId,
    String number,
    OrderStatus status,
    BigDecimal subtotal,
    BigDecimal discount,
    BigDecimal shippingFee,
    BigDecimal total,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<AdminOrderItemResponse> items,
    AdminOrderAddressResponse address,
    List<AdminOrderStatusHistoryResponse> statusHistory
) {
}
