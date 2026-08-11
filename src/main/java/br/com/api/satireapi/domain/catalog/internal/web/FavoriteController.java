package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.internal.dto.response.FavoriteResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.AddFavoriteUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.ListFavoritesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.RemoveFavoriteUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/favorites")
class FavoriteController {

    private final ListFavoritesUseCase listFavoritesUseCase;
    private final AddFavoriteUseCase addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;

    FavoriteController(
        ListFavoritesUseCase listFavoritesUseCase,
        AddFavoriteUseCase addFavoriteUseCase,
        RemoveFavoriteUseCase removeFavoriteUseCase
    ) {
        this.listFavoritesUseCase = listFavoritesUseCase;
        this.addFavoriteUseCase = addFavoriteUseCase;
        this.removeFavoriteUseCase = removeFavoriteUseCase;
    }

    @GetMapping
    PagedModel<FavoriteResponse> list(
        @AuthenticationPrincipal UUID customerId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PagedModel<>(listFavoritesUseCase.execute(customerId, pageable));
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void add(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID productId
    ) {
        addFavoriteUseCase.execute(customerId, productId);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void remove(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID productId
    ) {
        removeFavoriteUseCase.execute(customerId, productId);
    }
}
