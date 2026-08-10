package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.internal.dto.request.CreateCategoryRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.request.UpdateCategoryRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminCategoryResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.CreateCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.DeactivateCategoryUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.category.UpdateCategoryUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/categories")
class AdminCategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeactivateCategoryUseCase deactivateCategoryUseCase;

    AdminCategoryController(
        CreateCategoryUseCase createCategoryUseCase,
        UpdateCategoryUseCase updateCategoryUseCase,
        DeactivateCategoryUseCase deactivateCategoryUseCase
    ) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.updateCategoryUseCase = updateCategoryUseCase;
        this.deactivateCategoryUseCase = deactivateCategoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminCategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return createCategoryUseCase.execute(request);
    }

    @PatchMapping("/{categoryId}")
    AdminCategoryResponse update(
        @PathVariable UUID categoryId,
        @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return updateCategoryUseCase.execute(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable UUID categoryId) {
        deactivateCategoryUseCase.execute(categoryId);
    }
}
