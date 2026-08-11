package br.com.api.satireapi.domain.shipping;

import java.util.Optional;
import java.util.UUID;

public interface CustomerShipmentGateway {

    Optional<CustomerShipment> findByOrderId(UUID orderId);
}
