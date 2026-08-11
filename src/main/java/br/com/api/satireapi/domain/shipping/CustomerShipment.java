package br.com.api.satireapi.domain.shipping;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerShipment(
    UUID id,
    UUID orderId,
    String carrier,
    String trackingCode,
    String status,
    OffsetDateTime sentAt,
    OffsetDateTime deliveredAt,
    LocalDate estimatedDelivery
) {
}
