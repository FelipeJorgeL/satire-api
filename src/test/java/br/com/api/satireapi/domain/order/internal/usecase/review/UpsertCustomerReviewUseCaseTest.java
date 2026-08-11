package br.com.api.satireapi.domain.order.internal.usecase.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.CustomerReviewGateway;
import br.com.api.satireapi.domain.catalog.ProductReview;
import br.com.api.satireapi.domain.order.internal.dto.request.UpsertReviewRequest;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.usecase.ReviewPurchaseRequiredException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UpsertCustomerReviewUseCaseTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final CustomerReviewGateway reviewGateway = mock(CustomerReviewGateway.class);
    private final UpsertCustomerReviewUseCase useCase = new UpsertCustomerReviewUseCase(
        orderRepository, reviewGateway
    );

    @Test
    void createsReviewUsingDeliveredPurchaseOrder() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var request = new UpsertReviewRequest((short) 5, "Muito bom");
        var response = new ProductReview(
            UUID.randomUUID(), productId, (short) 5, "Muito bom",
            OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(orderRepository.findDeliveredPurchaseOrderId(customerId, productId))
            .thenReturn(Optional.of(orderId));
        when(reviewGateway.upsert(customerId, productId, orderId, (short) 5, "Muito bom"))
            .thenReturn(response);

        assertEquals(response, useCase.execute(customerId, productId, request));
    }

    @Test
    void rejectsReviewWithoutDeliveredPurchase() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var request = new UpsertReviewRequest((short) 4, null);
        when(orderRepository.findDeliveredPurchaseOrderId(customerId, productId))
            .thenReturn(Optional.empty());

        assertThrows(
            ReviewPurchaseRequiredException.class,
            () -> useCase.execute(customerId, productId, request)
        );
        verify(reviewGateway, never()).upsert(
            customerId, productId, null, (short) 4, null
        );
    }
}
