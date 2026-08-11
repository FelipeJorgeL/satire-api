package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.GetPublicCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicCategoriesUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
class CategoryController {
    private final ListPublicCategoriesUseCase listCategoriesUseCase;
    private final GetPublicCategoryUseCase getCategoryUseCase;

    CategoryController(
        ListPublicCategoriesUseCase listCategoriesUseCase,
        GetPublicCategoryUseCase getCategoryUseCase
    ) {
        this.listCategoriesUseCase = listCategoriesUseCase;
        this.getCategoryUseCase = getCategoryUseCase;
    }

    @GetMapping
    PagedModel<CategorySummary> list(
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "name")
        @Pattern(regexp = "name|createdAt") String sortBy,
        @RequestParam(defaultValue = "asc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return new PagedModel<>(listCategoriesUseCase.execute(pageable));
    }

    @GetMapping("/{categoryId}")
    CategorySummary get(@PathVariable UUID categoryId) {
        return getCategoryUseCase.execute(categoryId);
    }
}
