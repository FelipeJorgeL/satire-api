package br.com.api.satireapi.domain.shipping.internal.persistence;

import br.com.api.satireapi.domain.shipping.CustomerShipment;
import br.com.api.satireapi.domain.shipping.CustomerShipmentGateway;
import br.com.api.satireapi.domain.shipping.internal.model.Shipment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaCustomerShipmentGateway implements CustomerShipmentGateway {

    private final ShipmentRepository shipmentRepository;

    JpaCustomerShipmentGateway(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Optional<CustomerShipment> findByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId).map(this::toCustomerShipment);
    }

    private CustomerShipment toCustomerShipment(Shipment shipment) {
        return new CustomerShipment(
            shipment.getId(), shipment.getOrderId(), shipment.getCarrier(),
            shipment.getTrackingCode(), shipment.getStatus().name(), shipment.getSentAt(),
            shipment.getDeliveredAt(), shipment.getEstimatedDelivery()
        );
    }
}
