package br.com.api.satireapi.domain.customer.dto;

import java.util.UUID;

public record CustomerSummary(UUID id, String name, String email, boolean active) {
}
