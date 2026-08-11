package br.com.api.satireapi.domain.catalog;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryGateway {
    List<CategorySummary> findActive();
    Optional<CategorySummary> findActiveById(UUID id);
}
