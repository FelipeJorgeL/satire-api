package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.CategoryGateway;
import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.CategoryMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaCategoryGateaway implements CategoryGateway {
    private final JpaCategoryRepository repository;
    private final CategoryMapper mapper;

    public JpaCategoryGateaway(JpaCategoryRepository repository, CategoryMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<CategorySummary> findActiveById(UUID id) {
        return repository.findByIdAndActiveTrue(id).map(mapper::toSummary);
    }

    public List<CategorySummary> findActive() {
        return repository.findByActiveTrue().stream().map(mapper::toSummary).toList();
    }
}
