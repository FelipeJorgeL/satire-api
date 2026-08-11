package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.ProductGateway;
import br.com.api.satireapi.domain.catalog.dto.ProductSummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.ProductMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.usecase.InvalidIdException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JpaProductGateway implements ProductGateway {
    private final JpaProductRepository repository;
    private final ProductMapper mapper;

    public JpaProductGateway(JpaProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Optional<ProductSummary> findById(UUID id){
        if (id == null){
            throw new InvalidIdException();
        }
        return repository.findById(id).map(mapper::productToSummary);
    }

    public List<ProductSummary> findAll() {
        return repository.findAll().stream().map(mapper::productToSummary).toList();
    }

    public Optional<ProductSummary> findBySlug(String slug){
        return repository.findBySlug(slug).map(mapper::productToSummary);
    }
}
