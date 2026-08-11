package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.dto.CategorySummary;
import br.com.api.satireapi.domain.catalog.internal.dto.response.ProductResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.FindCategoryUsecase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {
    private final FindCategoryUsecase usecase;

    public CategoryController(FindCategoryUsecase usecase) {
        this.usecase = usecase;
    }

    @GetMapping
    public ResponseEntity<List<CategorySummary>> findActive(){
        return ResponseEntity.ok(usecase.findActive());
    }

    @GetMapping("{id}")
    public ResponseEntity<Optional<CategorySummary>> findActiveById(UUID id) {
        Optional<CategorySummary> response = usecase.findActiveById(id);

        return response.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }
}
