package br.com.api.satireapi.domain.shipping.internal.usecase.status;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.shipping.internal.dto.request.UpdateShipmentStatusRequest;
import br.com.api.satireapi.domain.shipping.internal.model.Shipment;
import br.com.api.satireapi.domain.shipping.internal.model.ShippingStatus;
import br.com.api.satireapi.domain.shipping.internal.persistence.ShipmentRepository;
import br.com.api.satireapi.domain.shipping.internal.usecase.ShipmentNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChangeShipmentStatusUseCaseTest {

    private final ShipmentRepository shipmentRepository = mock(ShipmentRepository.class);
    private final ChangeShipmentStatusUseCase useCase =
        new ChangeShipmentStatusUseCase(shipmentRepository);

    @Test
    void rejectsUnknownShipment() {
        var shipmentId = UUID.randomUUID();
        when(shipmentRepository.findByIdForUpdate(shipmentId)).thenReturn(Optional.empty());

        assertThrows(
            ShipmentNotFoundException.class,
            () -> useCase.execute(
                shipmentId, new UpdateShipmentStatusRequest(ShippingStatus.ENVIADO)
            )
        );
    }

    @Test
    void changesStatusUsingLockedShipment() {
        var shipment = Shipment.awaitingForOrder(UUID.randomUUID());
        var shipmentId = UUID.randomUUID();
        when(shipmentRepository.findByIdForUpdate(shipmentId)).thenReturn(Optional.of(shipment));

        useCase.execute(
            shipmentId, new UpdateShipmentStatusRequest(ShippingStatus.ENVIADO)
        );

        verify(shipmentRepository).findByIdForUpdate(shipmentId);
    }
}
