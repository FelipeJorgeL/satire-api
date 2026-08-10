package br.com.api.satireapi.domain.shipping.internal.mapper;

import br.com.api.satireapi.domain.shipping.internal.dto.response.AdminShipmentResponse;
import br.com.api.satireapi.domain.shipping.internal.model.Shipment;

public final class ShippingMapper {

    private ShippingMapper() {
    }

    public static AdminShipmentResponse toAdminResponse(Shipment shipment) {
        return new AdminShipmentResponse(
            shipment.getId(), shipment.getOrderId(), shipment.getCarrier(),
            shipment.getTrackingCode(), shipment.getStatus(), shipment.getSentAt(),
            shipment.getDeliveredAt(), shipment.getEstimatedDelivery(),
            shipment.getCreatedAt(), shipment.getUpdatedAt()
        );
    }
}
