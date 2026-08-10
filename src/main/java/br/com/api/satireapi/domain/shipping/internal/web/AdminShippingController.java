package br.com.api.satireapi.domain.shipping.internal.web;

import br.com.api.satireapi.domain.shipping.internal.dto.request.RegisterShipmentRequest;
import br.com.api.satireapi.domain.shipping.internal.dto.request.UpdateShipmentStatusRequest;
import br.com.api.satireapi.domain.shipping.internal.dto.response.AdminShipmentResponse;
import br.com.api.satireapi.domain.shipping.internal.usecase.details.CompleteShipmentDetailsUseCase;
import br.com.api.satireapi.domain.shipping.internal.usecase.status.ChangeShipmentStatusUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
class AdminShippingController {

    private final CompleteShipmentDetailsUseCase completeShipmentDetailsUseCase;
    private final ChangeShipmentStatusUseCase changeShipmentStatusUseCase;

    AdminShippingController(
        CompleteShipmentDetailsUseCase completeShipmentDetailsUseCase,
        ChangeShipmentStatusUseCase changeShipmentStatusUseCase
    ) {
        this.completeShipmentDetailsUseCase = completeShipmentDetailsUseCase;
        this.changeShipmentStatusUseCase = changeShipmentStatusUseCase;
    }

    @PostMapping("/orders/{orderId}/shipping")
    AdminShipmentResponse completeDetails(
        @PathVariable UUID orderId,
        @Valid @RequestBody RegisterShipmentRequest request
    ) {
        return completeShipmentDetailsUseCase.execute(orderId, request);
    }

    @PatchMapping("/shipments/{shipmentId}/status")
    AdminShipmentResponse changeStatus(
        @PathVariable UUID shipmentId,
        @Valid @RequestBody UpdateShipmentStatusRequest request
    ) {
        return changeShipmentStatusUseCase.execute(shipmentId, request);
    }
}
