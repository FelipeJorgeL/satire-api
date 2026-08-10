package br.com.api.satireapi.domain.inventory.internal.persistence;

import br.com.api.satireapi.domain.inventory.internal.dto.request.AdminStockMovementFilter;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovement;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class StockMovementSpecifications {

    private StockMovementSpecifications() {
    }

    public static Specification<StockMovement> from(AdminStockMovementFilter filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (filter.variationId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("variationId"), filter.variationId()));
            }
            if (filter.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), filter.type()));
            }
            if (filter.from() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.to()));
            }
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
