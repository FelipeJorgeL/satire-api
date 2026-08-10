package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.ProductVariation;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductVariationRepository extends JpaRepository<ProductVariation, UUID> {

    List<ProductVariation> findAllByProductIdOrderByCreatedAtAsc(UUID productId);

    void deleteAllByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select variation from ProductVariation variation where variation.id = :id")
    Optional<ProductVariation> findByIdForStockUpdate(@Param("id") UUID id);
}
