package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.catalog.internal.usecase.review.ListProductReviewsUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
class ProductReviewController {

    private final ListProductReviewsUseCase listProductReviewsUseCase;

    ProductReviewController(ListProductReviewsUseCase listProductReviewsUseCase) {
        this.listProductReviewsUseCase = listProductReviewsUseCase;
    }

    @GetMapping("/{productId}/reviews")
    PagedModel<ProductReview> list(
        @PathVariable UUID productId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PagedModel<>(listProductReviewsUseCase.execute(productId, pageable));
    }
}
