package br.com.api.satireapi.domain.catalog.dto;

import java.util.UUID;

public record ProductImageSummary(
        UUID id,
        String url,
        String alternativeText,
        boolean mainImage,
        int displayOrder
) {}