package br.com.api.satireapi.domain.order.internal.dto.request;

import br.com.api.satireapi.domain.order.OrderStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateOrderStatusRequest(
    @NotNull OrderStatus status,
    @Size(max = 255) String reason
) {

    @AssertTrue(message = "O cancelamento exige um motivo")
    public boolean hasReasonForCancellation() {
        return status != OrderStatus.CANCELADO || reason != null && !reason.isBlank();
    }
}
