package br.com.api.satireapi.domain.shipping.internal.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterShipmentRequest(
    @Size(min = 2, max = 100) String carrier,
    @Pattern(regexp = "[A-Za-z0-9._:/-]{3,120}") String trackingCode,
    @FutureOrPresent LocalDate estimatedDelivery
) {

    @AssertTrue(message = "Informe ao menos um dado da entrega")
    public boolean hasDetails() {
        return hasText(carrier) || hasText(trackingCode) || estimatedDelivery != null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
