package br.com.api.satireapi.domain.catalog.internal.usecase.review;

import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.catalog.internal.mapper.ReviewMapper;
import br.com.api.satireapi.domain.catalog.internal.persistence.ReviewRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.PublicProductFinder;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListProductReviewsUseCase {

    private final PublicProductFinder productFinder;
    private final ReviewRepository reviewRepository;

    public ListProductReviewsUseCase(
        PublicProductFinder productFinder,
        ReviewRepository reviewRepository
    ) {
        this.productFinder = productFinder;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductReview> execute(UUID productId, Pageable pageable) {
        productFinder.byId(productId);
        return reviewRepository.findAllByProductId(productId, pageable)
            .map(ReviewMapper::toResponse);
    }
}
