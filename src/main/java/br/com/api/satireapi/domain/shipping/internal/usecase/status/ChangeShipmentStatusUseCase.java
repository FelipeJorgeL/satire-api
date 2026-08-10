package br.com.api.satireapi.domain.shipping.internal.usecase.status;

import br.com.api.satireapi.domain.shipping.internal.dto.request.UpdateShipmentStatusRequest;
import br.com.api.satireapi.domain.shipping.internal.dto.response.AdminShipmentResponse;
import br.com.api.satireapi.domain.shipping.internal.mapper.ShippingMapper;
import br.com.api.satireapi.domain.shipping.internal.persistence.ShipmentRepository;
import br.com.api.satireapi.domain.shipping.internal.usecase.ShipmentNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeShipmentStatusUseCase {

    private final ShipmentRepository shipmentRepository;

    public ChangeShipmentStatusUseCase(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public AdminShipmentResponse execute(
        UUID shipmentId,
        UpdateShipmentStatusRequest request
    ) {
        var shipment = shipmentRepository.findByIdForUpdate(shipmentId)
            .orElseThrow(ShipmentNotFoundException::new);
        shipment.changeStatus(request.status());
        return ShippingMapper.toAdminResponse(shipment);
    }
}
