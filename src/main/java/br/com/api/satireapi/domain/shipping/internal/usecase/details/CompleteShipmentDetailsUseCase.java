package br.com.api.satireapi.domain.shipping.internal.usecase.details;

import br.com.api.satireapi.domain.shipping.internal.dto.request.RegisterShipmentRequest;
import br.com.api.satireapi.domain.shipping.internal.dto.response.AdminShipmentResponse;
import br.com.api.satireapi.domain.shipping.internal.mapper.ShippingMapper;
import br.com.api.satireapi.domain.shipping.internal.persistence.ShipmentRepository;
import br.com.api.satireapi.domain.shipping.internal.usecase.ShipmentNotFoundException;
import br.com.api.satireapi.domain.shipping.internal.usecase.TrackingCodeAlreadyExistsException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteShipmentDetailsUseCase {

    private final ShipmentRepository shipmentRepository;

    public CompleteShipmentDetailsUseCase(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public AdminShipmentResponse execute(
        UUID orderId,
        RegisterShipmentRequest request
    ) {
        var shipment = shipmentRepository.findByOrderIdForUpdate(orderId)
            .orElseThrow(ShipmentNotFoundException::new);
        var trackingCode = normalize(request.trackingCode());
        if (trackingCode != null
            && shipmentRepository.existsByTrackingCodeAndIdNot(trackingCode, shipment.getId())) {
            throw new TrackingCodeAlreadyExistsException();
        }
        shipment.updateDetails(request.carrier(), trackingCode, request.estimatedDelivery());
        return ShippingMapper.toAdminResponse(shipment);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        var normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
