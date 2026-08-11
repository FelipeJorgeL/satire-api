package br.com.api.satireapi.domain.order.internal.dto.response;

import java.util.UUID;

public record CustomerOrderAddressResponse(
    UUID id,
    String recipient,
    String postalCode,
    String street,
    String number,
    String complement,
    String neighborhood,
    String city,
    String state
) {
}
