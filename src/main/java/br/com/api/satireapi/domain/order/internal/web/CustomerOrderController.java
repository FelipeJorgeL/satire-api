package br.com.api.satireapi.domain.order.internal.web;

import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.request.CancelCustomerOrderRequest;
import br.com.api.satireapi.domain.order.internal.dto.request.CustomerOrderFilter;
import br.com.api.satireapi.domain.order.internal.dto.request.CreateOrderRequest;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderStatusHistoryResponse;
import br.com.api.satireapi.domain.order.internal.usecase.creation.CreateOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListCustomerOrderStatusHistoryUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListCustomerOrdersUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.status.CancelCustomerOrderUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
class CustomerOrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ListCustomerOrdersUseCase listCustomerOrdersUseCase;
    private final GetCustomerOrderUseCase getCustomerOrderUseCase;
    private final ListCustomerOrderStatusHistoryUseCase listCustomerOrderStatusHistoryUseCase;
    private final CancelCustomerOrderUseCase cancelCustomerOrderUseCase;

    CustomerOrderController(
        CreateOrderUseCase createOrderUseCase,
        ListCustomerOrdersUseCase listCustomerOrdersUseCase,
        GetCustomerOrderUseCase getCustomerOrderUseCase,
        ListCustomerOrderStatusHistoryUseCase listCustomerOrderStatusHistoryUseCase,
        CancelCustomerOrderUseCase cancelCustomerOrderUseCase
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.listCustomerOrdersUseCase = listCustomerOrdersUseCase;
        this.getCustomerOrderUseCase = getCustomerOrderUseCase;
        this.listCustomerOrderStatusHistoryUseCase = listCustomerOrderStatusHistoryUseCase;
        this.cancelCustomerOrderUseCase = cancelCustomerOrderUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerOrderResponse create(
        @AuthenticationPrincipal UUID customerId,
        @Valid @RequestBody CreateOrderRequest request
    ) {
        return createOrderUseCase.execute(customerId, request);
    }

    @GetMapping
    PagedModel<CustomerOrderResponse> list(
        @AuthenticationPrincipal UUID customerId,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "number|status|total|createdAt|updatedAt") String sortBy,
        @RequestParam(defaultValue = "desc")
        @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(
            page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)
        );
        return new PagedModel<>(listCustomerOrdersUseCase.execute(
            customerId, new CustomerOrderFilter(status, from, to), pageable
        ));
    }

    @GetMapping("/{orderId}")
    CustomerOrderDetailsResponse get(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId
    ) {
        return getCustomerOrderUseCase.execute(customerId, orderId);
    }

    @GetMapping("/{orderId}/status-history")
    List<CustomerOrderStatusHistoryResponse> statusHistory(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId
    ) {
        return listCustomerOrderStatusHistoryUseCase.execute(customerId, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void cancel(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId,
        @Valid @RequestBody CancelCustomerOrderRequest request
    ) {
        cancelCustomerOrderUseCase.execute(customerId, orderId, request);
    }
}
