package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListPublicCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    public ListPublicCategoriesUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategorySummary> execute(Pageable pageable) {
        return categoryRepository.findAllByActiveTrue(pageable)
            .map(PublicCatalogMapper::toCategory);
    }
}
