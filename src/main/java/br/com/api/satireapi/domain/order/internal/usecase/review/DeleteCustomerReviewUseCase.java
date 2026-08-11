package br.com.api.satireapi.domain.order.internal.usecase.review;

import br.com.api.satireapi.domain.catalog.CustomerReviewGateway;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteCustomerReviewUseCase {

    private final CustomerReviewGateway reviewGateway;

    public DeleteCustomerReviewUseCase(CustomerReviewGateway reviewGateway) {
        this.reviewGateway = reviewGateway;
    }

    @Transactional
    public void execute(UUID customerId, UUID productId) {
        reviewGateway.delete(customerId, productId);
    }
}
