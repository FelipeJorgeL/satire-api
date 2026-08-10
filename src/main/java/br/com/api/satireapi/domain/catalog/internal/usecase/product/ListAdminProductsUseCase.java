package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.dto.request.AdminProductFilter;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.CatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Product;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAdminProductsUseCase {

    private final ProductRepository productRepository;

    public ListAdminProductsUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminProductListItemResponse> execute(AdminProductFilter filter, Pageable pageable) {
        return productRepository.findAll(specification(filter), pageable).map(CatalogMapper::toListItem);
    }

    private Specification<Product> specification(AdminProductFilter filter) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();
            if (filter.search() != null && !filter.search().isBlank()) {
                var search = "%" + escapeLike(filter.search().trim().toLowerCase(Locale.ROOT)) + "%";
                var name = builder.like(builder.lower(root.get("name")), search, '\\');
                var slug = builder.like(builder.lower(root.get("slug")), search, '\\');
                predicates.add(builder.or(name, slug));
            }
            if (filter.active() != null) {
                predicates.add(builder.equal(root.get("active"), filter.active()));
            }
            if (filter.categoryId() != null) {
                predicates.add(builder.equal(root.get("categoryId"), filter.categoryId()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
