package br.com.felipejorge.satireapi.domain.customer.dto;

import java.util.UUID;

public record CustomerSummary(UUID id, String name, String email, boolean active) {
}
