package br.com.api.satireapi.domain.order.internal.web;

import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.order.internal.dto.request.UpsertReviewRequest;
import br.com.api.satireapi.domain.order.internal.usecase.review.DeleteCustomerReviewUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.review.UpsertCustomerReviewUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/reviews")
class CustomerReviewController {

    private final UpsertCustomerReviewUseCase upsertReviewUseCase;
    private final DeleteCustomerReviewUseCase deleteReviewUseCase;

    CustomerReviewController(
        UpsertCustomerReviewUseCase upsertReviewUseCase,
        DeleteCustomerReviewUseCase deleteReviewUseCase
    ) {
        this.upsertReviewUseCase = upsertReviewUseCase;
        this.deleteReviewUseCase = deleteReviewUseCase;
    }

    @PutMapping("/{productId}")
    ProductReview upsert(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID productId,
        @Valid @RequestBody UpsertReviewRequest request
    ) {
        return upsertReviewUseCase.execute(customerId, productId, request);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID productId
    ) {
        deleteReviewUseCase.execute(customerId, productId);
    }
}
