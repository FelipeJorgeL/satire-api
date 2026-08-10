package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.UUID;

public record ConfirmationEmailQueuedEvent(UUID outboxId) {
}
