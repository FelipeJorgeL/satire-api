package br.com.api.satireapi.domain.catalog.internal.usecase.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.internal.model.Review;
import br.com.api.satireapi.domain.catalog.internal.persistence.ReviewRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.PublicProductFinder;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ListProductReviewsUseCaseTest {

    @Test
    void listsOnlyReviewsForVisibleProduct() {
        var productId = UUID.randomUUID();
        var productFinder = mock(PublicProductFinder.class);
        var reviewRepository = mock(ReviewRepository.class);
        var review = mock(Review.class);
        var pageable = PageRequest.of(0, 20);
        when(review.getId()).thenReturn(UUID.randomUUID());
        when(review.getProductId()).thenReturn(productId);
        when(review.getRating()).thenReturn((short) 5);
        when(review.getCreatedAt()).thenReturn(OffsetDateTime.now());
        when(review.getUpdatedAt()).thenReturn(OffsetDateTime.now());
        when(reviewRepository.findAllByProductId(productId, pageable))
            .thenReturn(new PageImpl<>(java.util.List.of(review), pageable, 1));
        var useCase = new ListProductReviewsUseCase(productFinder, reviewRepository);

        var response = useCase.execute(productId, pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals((short) 5, response.getContent().get(0).rating());
        verify(productFinder).byId(productId);
    }
}
