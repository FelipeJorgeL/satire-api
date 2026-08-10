package br.com.api.satireapi.domain.customer.internal.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerAddressResponse(
    UUID id,
    String label,
    String recipient,
    String postalCode,
    String street,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state,
    boolean primary,
    OffsetDateTime createdAt
) {
}
