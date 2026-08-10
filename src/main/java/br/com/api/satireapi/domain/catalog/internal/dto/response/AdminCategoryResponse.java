package br.com.api.satireapi.domain.catalog.internal.dto.response;

import java.util.UUID;

public record AdminCategoryResponse(UUID id, String name, String slug, boolean active) {
}
