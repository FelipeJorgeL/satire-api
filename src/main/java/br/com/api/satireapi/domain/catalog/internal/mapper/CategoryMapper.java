package br.com.api.satireapi.domain.catalog.internal.mapper;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.model.Category;

public class CategoryMapper {

    public CategorySummary toSummary(Category category) {
        return new CategorySummary(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }

}
