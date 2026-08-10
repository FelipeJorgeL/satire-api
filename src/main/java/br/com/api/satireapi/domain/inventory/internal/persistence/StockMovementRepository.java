package br.com.api.satireapi.domain.inventory.internal.persistence;

import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockMovementRepository
    extends JpaRepository<StockMovement, UUID>, JpaSpecificationExecutor<StockMovement> {

    List<StockMovement> findTop20ByVariationIdOrderByCreatedAtDesc(UUID variationId);

    @Query("""
        select coalesce(sum(m.quantity), 0)
        from StockMovement m
        where m.variationId = :variationId and m.type = :type
        """)
    long sumQuantityByVariationIdAndType(
        @Param("variationId") UUID variationId,
        @Param("type") StockMovementType type
    );
}
