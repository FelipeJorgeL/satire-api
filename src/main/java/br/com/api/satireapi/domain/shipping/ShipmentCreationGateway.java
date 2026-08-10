package br.com.api.satireapi.domain.shipping;

import java.util.UUID;
import java.time.LocalDate;

public interface ShipmentCreationGateway {

    void ensureForOrder(UUID orderId, LocalDate estimatedDelivery);
}
