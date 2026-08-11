package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaVariationRepository extends JpaRepository<ProductVariation, UUID> {
    List<ProductVariation> findByProductIdAndActiveTrue(UUID id);
}
