package br.com.api.satireapi.domain.catalog.internal.usecase.category;

import br.com.api.satireapi.domain.catalog.internal.dto.request.UpdateCategoryRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminCategoryResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.CatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public UpdateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public AdminCategoryResponse execute(UUID categoryId, UpdateCategoryRequest request) {
        var category = categoryRepository.findByIdForUpdate(categoryId)
            .orElseThrow(CategoryNotFoundException::new);
        var slug = request.slug() == null ? null : request.slug().trim();
        if (slug != null && categoryRepository.existsBySlugAndIdNot(slug, categoryId)) {
            throw new CategorySlugAlreadyExistsException();
        }
        category.update(request.name(), slug);
        return CatalogMapper.toResponse(category);
    }
}
