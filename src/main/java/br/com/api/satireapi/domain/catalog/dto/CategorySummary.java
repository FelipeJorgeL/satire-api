package br.com.api.satireapi.domain.catalog.dto;

import java.util.UUID;

public record CategorySummary(
        UUID id,
        String name,
        String slug
) {}
