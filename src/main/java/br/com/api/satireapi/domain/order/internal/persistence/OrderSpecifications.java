package br.com.api.satireapi.domain.order.internal.persistence;

import br.com.api.satireapi.domain.order.internal.dto.request.AdminOrderFilter;
import br.com.api.satireapi.domain.order.internal.model.Order;
import java.util.ArrayList;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> from(AdminOrderFilter filter) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (filter.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.status()));
            }
            if (filter.customerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("customerId"), filter.customerId()));
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
