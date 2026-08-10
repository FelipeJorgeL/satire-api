package br.com.api.satireapi.domain.catalog.internal.dto.request;

import java.util.UUID;

public record AdminProductFilter(String search, Boolean active, UUID categoryId) {
}
