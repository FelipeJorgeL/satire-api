package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(@NotNull Boolean active) {
}
