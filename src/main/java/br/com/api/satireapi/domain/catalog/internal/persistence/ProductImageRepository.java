package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    List<ProductImage> findAllByProductIdOrderByDisplayOrderAsc(UUID productId);

    Optional<ProductImage> findByIdAndProductId(UUID id, UUID productId);

    void deleteAllByProductId(UUID productId);
}
