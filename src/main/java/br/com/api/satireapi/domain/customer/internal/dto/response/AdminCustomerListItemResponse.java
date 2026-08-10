package br.com.api.satireapi.domain.customer.internal.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminCustomerListItemResponse(
    UUID id,
    String name,
    String email,
    boolean active,
    OffsetDateTime createdAt
) {
}
