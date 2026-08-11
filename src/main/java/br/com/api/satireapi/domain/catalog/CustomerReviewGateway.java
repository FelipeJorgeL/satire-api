package br.com.api.satireapi.domain.catalog;

import java.util.UUID;

public interface CustomerReviewGateway {

    ProductReview upsert(
        UUID customerId,
        UUID productId,
        UUID orderId,
        short rating,
        String comment
    );

    void delete(UUID customerId, UUID productId);
}
