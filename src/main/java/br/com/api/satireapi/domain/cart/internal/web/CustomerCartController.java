package br.com.api.satireapi.domain.cart.internal.web;

import br.com.api.satireapi.domain.cart.internal.dto.request.AddCartItemRequest;
import br.com.api.satireapi.domain.cart.internal.dto.request.UpdateCartItemRequest;
import br.com.api.satireapi.domain.cart.internal.dto.response.CartResponse;
import br.com.api.satireapi.domain.cart.internal.usecase.item.AddCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.ClearCartUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.RemoveCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.item.UpdateCartItemUseCase;
import br.com.api.satireapi.domain.cart.internal.usecase.query.GetCartUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/cart")
class CustomerCartController {

    private final GetCartUseCase getCartUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;

    CustomerCartController(
        GetCartUseCase getCartUseCase,
        AddCartItemUseCase addCartItemUseCase,
        UpdateCartItemUseCase updateCartItemUseCase,
        RemoveCartItemUseCase removeCartItemUseCase,
        ClearCartUseCase clearCartUseCase
    ) {
        this.getCartUseCase = getCartUseCase;
        this.addCartItemUseCase = addCartItemUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
    }

    @GetMapping
    CartResponse get(@AuthenticationPrincipal UUID customerId) {
        return getCartUseCase.execute(customerId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    CartResponse addItem(
        @AuthenticationPrincipal UUID customerId,
        @Valid @RequestBody AddCartItemRequest request
    ) {
        return addCartItemUseCase.execute(customerId, request);
    }

    @PatchMapping("/items/{itemId}")
    CartResponse updateItem(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID itemId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return updateCartItemUseCase.execute(customerId, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeItem(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID itemId
    ) {
        removeCartItemUseCase.execute(customerId, itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void clear(@AuthenticationPrincipal UUID customerId) {
        clearCartUseCase.execute(customerId);
    }
}
