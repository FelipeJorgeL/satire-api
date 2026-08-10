package br.com.api.satireapi.domain.shipping.internal.dto.request;

import br.com.api.satireapi.domain.shipping.internal.model.ShippingStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(@NotNull ShippingStatus status) {
}
