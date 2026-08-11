package br.com.api.satireapi.domain.catalog.internal.dto.request;

import java.util.UUID;

public record PublicProductFilter(String search, UUID categoryId) {
}
