package br.com.api.satireapi.domain.order.internal.web;

import br.com.api.satireapi.domain.order.internal.dto.request.CreateOrderRequest;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderResponse;
import br.com.api.satireapi.domain.order.internal.usecase.creation.CreateOrderUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
class CustomerOrderController {

    private final CreateOrderUseCase createOrderUseCase;

    CustomerOrderController(CreateOrderUseCase createOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerOrderResponse create(
        @AuthenticationPrincipal UUID customerId,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        return createOrderUseCase.execute(customerId, request);
    }
}
