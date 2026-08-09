package br.com.api.satireapi.domain.customer.internal.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateAdminCustomerStatusRequest(@NotNull Boolean active) {
}
