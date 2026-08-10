package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;
import br.com.api.satireapi.domain.catalog.internal.dto.response.ProductResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.FindImageUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.FindProductUsecase;
import br.com.api.satireapi.domain.catalog.internal.usecase.FindVariationUsecase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.condition.ProducesRequestCondition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {
    private final FindProductUsecase usecase;
    private final FindVariationUsecase variationUsecase;
    private final FindImageUseCase imageUseCase;

    ProductController(FindProductUsecase usecase, FindVariationUsecase variationUsecase, FindImageUseCase imageUseCase) {
        this.usecase = usecase;
        this.variationUsecase = variationUsecase;
        this.imageUseCase = imageUseCase;
    }

    @GetMapping()
    ResponseEntity<List<ProductResponse>> findAll(){
        return ResponseEntity.ok(usecase.findAll());
    }

    @GetMapping("{id}")
    ResponseEntity<ProductResponse> findById(@PathVariable UUID id){
        Optional<ProductResponse> response = usecase.findById(id);

        return response.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(response.get());
    }

    @GetMapping("slug/{slug}")
    ResponseEntity<ProductResponse> findBySlug(@PathVariable String slug){
        Optional<ProductResponse> response = usecase.findBySlug(slug);

        return response.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(response.get());
    }

    @GetMapping("{productId}/variations")
    ResponseEntity<List<ProductVariationSummary> > findVariationByProductId(@PathVariable UUID id){
        List<ProductVariationSummary> response = variationUsecase.findVariationByProductId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{productId}/images")
    ResponseEntity<List<ProductImageSummary> > findImageByProductId(@PathVariable UUID id){
        List<ProductImageSummary> response = imageUseCase.findActiveByProductId(id);
        return ResponseEntity.ok(response);
    }
}
