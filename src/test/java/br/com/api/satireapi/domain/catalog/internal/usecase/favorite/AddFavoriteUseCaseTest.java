package br.com.api.satireapi.domain.catalog.internal.usecase.favorite;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.catalog.internal.persistence.FavoriteRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ProductNotFoundException;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.PublicProductFinder;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AddFavoriteUseCaseTest {

    private PublicProductFinder productFinder;
    private FavoriteRepository favoriteRepository;
    private AddFavoriteUseCase useCase;

    @BeforeEach
    void setUp() {
        productFinder = mock(PublicProductFinder.class);
        favoriteRepository = mock(FavoriteRepository.class);
        useCase = new AddFavoriteUseCase(productFinder, favoriteRepository);
    }

    @Test
    void addsOnlyVisibleProductForAuthenticatedCustomer() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        useCase.execute(customerId, productId);

        verify(productFinder).byId(productId);
        verify(favoriteRepository).addIfAbsent(customerId, productId);
    }

    @Test
    void doesNotAddHiddenProduct() {
        var customerId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        when(productFinder.byId(productId)).thenThrow(new ProductNotFoundException());

        assertThrows(ProductNotFoundException.class, () -> useCase.execute(customerId, productId));

        verify(favoriteRepository, never()).addIfAbsent(customerId, productId);
    }
}
