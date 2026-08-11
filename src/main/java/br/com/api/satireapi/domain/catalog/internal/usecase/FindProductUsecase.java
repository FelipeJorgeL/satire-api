package br.com.api.satireapi.domain.catalog.internal.usecase;

import br.com.api.satireapi.domain.catalog.ProductGateway;
import br.com.api.satireapi.domain.catalog.internal.dto.response.ProductResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.ProductMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FindProductUsecase {

    private final ProductGateway gateway;
    private final ProductMapper mapper;

    public FindProductUsecase(ProductMapper mapper, ProductGateway gateway) {
        this.mapper = mapper;
        this.gateway = gateway;
    }

    public Optional<ProductResponse> findById(UUID id) {
        return gateway.findById(id)
                .map(mapper::summaryToResponse);
    }

    public List<ProductResponse> findAll() {
        return gateway.findAll()
                .stream().map(mapper::summaryToResponse).toList();
    }

    public Optional<ProductResponse> findBySlug(String slug) {
        return gateway.findBySlug(slug)
                .map(mapper::summaryToResponse);
    }
}


