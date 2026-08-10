package br.com.api.satireapi.domain.shipping.internal.persistence;

import br.com.api.satireapi.domain.shipping.ShipmentCreationGateway;
import br.com.api.satireapi.domain.shipping.internal.model.Shipment;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
class JpaShipmentCreationGateway implements ShipmentCreationGateway {

    private final ShipmentRepository shipmentRepository;

    JpaShipmentCreationGateway(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    @Transactional
    public void ensureForOrder(UUID orderId, LocalDate estimatedDelivery) {
        if (orderId == null) {
            throw new IllegalArgumentException("O pedido da entrega Ã© obrigatÃ³rio");
        }
        if (shipmentRepository.findByOrderId(orderId).isEmpty()) {
            shipmentRepository.save(Shipment.awaitingForOrder(orderId, estimatedDelivery));
        }
    }
}
