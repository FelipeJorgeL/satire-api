package br.com.api.satireapi.domain.order.internal.usecase.review;

import br.com.api.satireapi.domain.catalog.CustomerReviewGateway;
import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.order.internal.dto.request.UpsertReviewRequest;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.usecase.ReviewPurchaseRequiredException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpsertCustomerReviewUseCase {

    private final OrderRepository orderRepository;
    private final CustomerReviewGateway reviewGateway;

    public UpsertCustomerReviewUseCase(
        OrderRepository orderRepository,
        CustomerReviewGateway reviewGateway
    ) {
        this.orderRepository = orderRepository;
        this.reviewGateway = reviewGateway;
    }

    @Transactional
    public ProductReview execute(
        UUID customerId,
        UUID productId,
        UpsertReviewRequest request
    ) {
        var orderId = orderRepository.findDeliveredPurchaseOrderId(customerId, productId)
            .orElseThrow(ReviewPurchaseRequiredException::new);
        return reviewGateway.upsert(
            customerId, productId, orderId, request.rating(), request.comment()
        );
    }
}
