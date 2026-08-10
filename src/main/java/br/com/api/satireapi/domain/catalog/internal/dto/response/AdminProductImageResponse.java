package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.util.UUID;

public record AdminProductImageResponse(
    UUID id,
    String url,
    String altText,
    boolean decorative,
    boolean primary,
    int displayOrder
) {
}
