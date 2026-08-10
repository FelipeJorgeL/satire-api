package br.com.api.satireapi.domain.customer;

import java.util.UUID;

public record ConfirmationEmailDelivery(
    UUID id,
    String recipient,
    String protectedLink,
    int attempts
) {
}
