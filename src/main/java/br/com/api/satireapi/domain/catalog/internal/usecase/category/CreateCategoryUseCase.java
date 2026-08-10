package br.com.api.satireapi.domain.catalog.internal.usecase.category;

import br.com.api.satireapi.domain.catalog.internal.dto.request.CreateCategoryRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminCategoryResponse;
import br.com.api.satireapi.domain.catalog.internal.mapper.CatalogMapper;
import br.com.api.satireapi.domain.catalog.internal.model.Category;
import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public CreateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public AdminCategoryResponse execute(CreateCategoryRequest request) {
        var slug = request.slug().trim();
        if (categoryRepository.existsBySlug(slug)) {
            throw new CategorySlugAlreadyExistsException();
        }
        return CatalogMapper.toResponse(categoryRepository.save(
            Category.create(request.name(), slug)
        ));
    }
}
