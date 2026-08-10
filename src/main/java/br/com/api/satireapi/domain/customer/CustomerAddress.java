package br.com.api.satireapi.domain.customer;

import java.util.UUID;

public record CustomerAddress(
    UUID id,
    UUID customerId,
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
