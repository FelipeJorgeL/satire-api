package br.com.api.satireapi.domain.customer;

import java.util.Set;
import java.util.UUID;

public record CustomerAuthentication(UUID id, boolean active, Set<String> profiles) {
}
