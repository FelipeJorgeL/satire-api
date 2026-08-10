package br.com.api.satireapi.domain.order.internal.web;

import br.com.api.satireapi.domain.order.OrderStatus;
import br.com.api.satireapi.domain.order.internal.dto.request.AdminOrderFilter;
import br.com.api.satireapi.domain.order.internal.dto.request.UpdateOrderStatusRequest;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderDetailsResponse;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderListItemResponse;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetAdminOrderUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.query.ListAdminOrdersUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.status.ChangeAdminOrderStatusUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
class AdminOrderController {

    private final ListAdminOrdersUseCase listAdminOrdersUseCase;
    private final GetAdminOrderUseCase getAdminOrderUseCase;
    private final ChangeAdminOrderStatusUseCase changeAdminOrderStatusUseCase;

    AdminOrderController(
        ListAdminOrdersUseCase listAdminOrdersUseCase,
        GetAdminOrderUseCase getAdminOrderUseCase,
        ChangeAdminOrderStatusUseCase changeAdminOrderStatusUseCase
    ) {
        this.listAdminOrdersUseCase = listAdminOrdersUseCase;
        this.getAdminOrderUseCase = getAdminOrderUseCase;
        this.changeAdminOrderStatusUseCase = changeAdminOrderStatusUseCase;
    }

    @GetMapping
    PagedModel<AdminOrderListItemResponse> list(
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) UUID customerId,
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "number|status|total|createdAt|updatedAt") String sortBy,
        @RequestParam(defaultValue = "desc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(
            page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)
        );
        return new PagedModel<>(listAdminOrdersUseCase.execute(
            new AdminOrderFilter(status, customerId, from, to), pageable
        ));
    }

    @GetMapping("/{orderId}")
    AdminOrderDetailsResponse get(@PathVariable UUID orderId) {
        return getAdminOrderUseCase.execute(orderId);
    }

    @PatchMapping("/{orderId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeStatus(
        @AuthenticationPrincipal UUID adminId,
        @PathVariable UUID orderId,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        changeAdminOrderStatusUseCase.execute(adminId, orderId, request);
    }
}
