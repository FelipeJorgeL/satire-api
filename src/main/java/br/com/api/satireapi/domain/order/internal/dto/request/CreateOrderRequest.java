package br.com.api.satireapi.domain.order.internal.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateOrderRequest(@NotNull UUID addressId) {
}
