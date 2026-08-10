package br.com.api.satireapi.domain.shipping.internal.dto.response;

import br.com.api.satireapi.domain.shipping.internal.model.ShippingStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminShipmentResponse(
    UUID id,
    UUID orderId,
    String carrier,
    String trackingCode,
    ShippingStatus status,
    OffsetDateTime sentAt,
    OffsetDateTime deliveredAt,
    LocalDate estimatedDelivery,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
