package br.com.api.satireapi.domain.catalog.internal.usecase;

import br.com.api.satireapi.domain.catalog.CategoryGateway;
import br.com.api.satireapi.domain.catalog.dto.CategorySummary;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FindCategoryUsecase {
    private final CategoryGateway gateway;

    public FindCategoryUsecase(CategoryGateway gateway) {
        this.gateway = gateway;
    }

    public Optional<CategorySummary> findActiveById(UUID id) {
        if (id == null) {
            throw new InvalidIdException();
        }
        return gateway.findActiveById(id);
    }

    public List<CategorySummary> findActive() { return gateway.findActive(); }
}
