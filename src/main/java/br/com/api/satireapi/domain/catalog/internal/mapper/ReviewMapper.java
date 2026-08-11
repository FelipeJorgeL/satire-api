package br.com.api.satireapi.domain.catalog.internal.mapper;

import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.catalog.internal.model.Review;

public final class ReviewMapper {

    private ReviewMapper() {
    }

    public static ProductReview toResponse(Review review) {
        return new ProductReview(
            review.getId(), review.getProductId(), review.getRating(), review.getComment(),
            review.getCreatedAt(), review.getUpdatedAt()
        );
    }
}
