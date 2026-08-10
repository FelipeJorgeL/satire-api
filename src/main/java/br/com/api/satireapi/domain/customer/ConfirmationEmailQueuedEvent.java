package br.com.api.satireapi.domain.customer;

import java.util.UUID;

public record ConfirmationEmailQueuedEvent(UUID outboxId) {
}
