package br.com.api.satireapi.domain.catalog.internal.usecase.category;

import br.com.api.satireapi.domain.catalog.internal.persistence.CategoryRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    public DeactivateCategoryUseCase(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public void execute(UUID categoryId) {
        var category = categoryRepository.findByIdForUpdate(categoryId)
            .orElseThrow(CategoryNotFoundException::new);
        category.changeStatus(false);
    }
}
