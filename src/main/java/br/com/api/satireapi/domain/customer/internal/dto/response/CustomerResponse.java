package br.com.api.satireapi.domain.customer.internal.dto.response;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String name,
    String email,
    String cpf,
    String phone,
    boolean active,
    Set<String> profiles,
    OffsetDateTime createdAt
) {
}
