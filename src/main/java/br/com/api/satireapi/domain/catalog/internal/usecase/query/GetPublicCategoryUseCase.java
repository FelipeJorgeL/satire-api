package br.com.api.satireapi.domain.catalog.internal.usecase.query;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.mapper.PublicCatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.CategoryNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetPublicCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public GetPublicCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public CategorySummary execute(UUID categoryId) {
        return categoryRepository.findByIdAndActiveTrue(categoryId)
            .map(PublicCatalogMapper::toCategory)
            .orElseThrow(CategoryNotFoundException::new);
    }
}
