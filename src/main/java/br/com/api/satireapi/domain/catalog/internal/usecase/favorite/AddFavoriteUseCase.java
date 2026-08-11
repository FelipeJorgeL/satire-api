package br.com.api.satireapi.domain.catalog.internal.usecase.favorite;

import br.com.api.satireapi.domain.catalog.internal.persistence.FavoriteRepository;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.PublicProductFinder;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddFavoriteUseCase {

    private final PublicProductFinder productFinder;
    private final FavoriteRepository favoriteRepository;

    public AddFavoriteUseCase(
        PublicProductFinder productFinder,
        FavoriteRepository favoriteRepository
    ) {
        this.productFinder = productFinder;
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional
    public void execute(UUID customerId, UUID productId) {
        productFinder.byId(productId);
        favoriteRepository.addIfAbsent(customerId, productId);
    }
}
