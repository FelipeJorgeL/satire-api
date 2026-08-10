package br.com.api.satireapi.domain.catalog;

import br.com.api.satireapi.domain.catalog.dto.ProductSummary;
import br.com.api.satireapi.domain.catalog.internal.dto.response.ProductResponse;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductGateway {
    List<ProductSummary> findAll();
    Optional<ProductSummary> findById(UUID id);
    Optional<ProductSummary> findBySlug(String slug);
}
