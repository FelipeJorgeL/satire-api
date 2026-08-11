package br.com.api.satireapi.domain.catalog.internal.usecase.favorite;

import br.com.api.satireapi.domain.catalog.internal.persistence.FavoriteRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveFavoriteUseCase {

    private final FavoriteRepository favoriteRepository;

    public RemoveFavoriteUseCase(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional
    public void execute(UUID customerId, UUID productId) {
        favoriteRepository.deleteForCustomer(customerId, productId);
    }
}
