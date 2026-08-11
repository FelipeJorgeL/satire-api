package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaImageRepository extends JpaRepository<ProductImage, UUID> {
    List<ProductImage> findByProductIdAndActiveTrueOrderByDisplayOrder(UUID id);
}
