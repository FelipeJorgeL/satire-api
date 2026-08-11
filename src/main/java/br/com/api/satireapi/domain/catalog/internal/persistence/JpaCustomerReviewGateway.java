package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.CustomerReviewGateway;
import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.catalog.internal.mapper.ReviewMapper;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.PublicProductFinder;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class JpaCustomerReviewGateway implements CustomerReviewGateway {

    private static final int MAX_COMMENT_LENGTH = 2_000;

    private final PublicProductFinder productFinder;
    private final ReviewRepository reviewRepository;

    JpaCustomerReviewGateway(
        PublicProductFinder productFinder,
        ReviewRepository reviewRepository
    ) {
        this.productFinder = productFinder;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public ProductReview upsert(
        UUID customerId,
        UUID productId,
        UUID orderId,
        short rating,
        String comment
    ) {
        validate(customerId, productId, orderId, rating, comment);
        productFinder.byId(productId);
        var normalizedComment = normalize(comment);
        reviewRepository.upsert(
            UUID.randomUUID(), customerId, productId, orderId, rating, normalizedComment
        );
        return reviewRepository.findByCustomerIdAndProductId(customerId, productId)
            .map(ReviewMapper::toResponse)
            .orElseThrow(() -> new IllegalStateException("Avaliação não foi persistida"));
    }

    @Override
    public void delete(UUID customerId, UUID productId) {
        if (customerId == null || productId == null) {
            throw new IllegalArgumentException("Cliente e produto são obrigatórios");
        }
        reviewRepository.deleteOwned(customerId, productId);
    }

    private static void validate(
        UUID customerId,
        UUID productId,
        UUID orderId,
        short rating,
        String comment
    ) {
        if (customerId == null || productId == null || orderId == null
            || rating < 1 || rating > 5
            || (comment != null && comment.trim().length() > MAX_COMMENT_LENGTH)) {
            throw new IllegalArgumentException("Dados da avaliação são inválidos");
        }
    }

    private static String normalize(String comment) {
        if (comment == null) {
            return null;
        }
        var normalized = comment.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
