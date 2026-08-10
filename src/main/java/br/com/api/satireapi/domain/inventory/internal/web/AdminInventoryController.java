package br.com.api.satireapi.domain.inventory.internal.web;

import br.com.api.satireapi.domain.inventory.internal.dto.request.AdminStockMovementFilter;
import br.com.api.satireapi.domain.inventory.internal.dto.request.RegisterStockMovementRequest;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockMovementResponse;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockVariationResponse;
import br.com.api.satireapi.domain.inventory.internal.model.StockMovementType;
import br.com.api.satireapi.domain.inventory.internal.usecase.movement.ListAdminStockMovementsUseCase;
import br.com.api.satireapi.domain.inventory.internal.usecase.movement.RegisterStockMovementUseCase;
import br.com.api.satireapi.domain.inventory.internal.usecase.variation.GetAdminStockVariationUseCase;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/inventory")
class AdminInventoryController {

    private final RegisterStockMovementUseCase registerStockMovementUseCase;
    private final ListAdminStockMovementsUseCase listAdminStockMovementsUseCase;
    private final GetAdminStockVariationUseCase getAdminStockVariationUseCase;

    AdminInventoryController(
        RegisterStockMovementUseCase registerStockMovementUseCase,
        ListAdminStockMovementsUseCase listAdminStockMovementsUseCase,
        GetAdminStockVariationUseCase getAdminStockVariationUseCase
    ) {
        this.registerStockMovementUseCase = registerStockMovementUseCase;
        this.listAdminStockMovementsUseCase = listAdminStockMovementsUseCase;
        this.getAdminStockVariationUseCase = getAdminStockVariationUseCase;
    }

    @PostMapping("/movements")
    @ResponseStatus(HttpStatus.CREATED)
    AdminStockMovementResponse registerMovement(
        @AuthenticationPrincipal UUID adminId,
        @Valid @RequestBody RegisterStockMovementRequest request
    ) {
        return registerStockMovementUseCase.execute(adminId, request);
    }

    @GetMapping("/movements")
    PagedModel<AdminStockMovementResponse> listMovements(
        @RequestParam(required = false) UUID variationId,
        @RequestParam(required = false) StockMovementType type,
        @RequestParam(required = false) OffsetDateTime from,
        @RequestParam(required = false) OffsetDateTime to,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "createdAt|quantity|type|previousStock|newStock") String sortBy,
        @RequestParam(defaultValue = "desc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(
            page, size, Sort.by(Sort.Direction.fromString(direction), sortBy)
        );
        return new PagedModel<>(listAdminStockMovementsUseCase.execute(
            new AdminStockMovementFilter(variationId, type, from, to), pageable
        ));
    }

    @GetMapping("/variations/{variationId}")
    AdminStockVariationResponse getVariation(@PathVariable UUID variationId) {
        return getAdminStockVariationUseCase.execute(variationId);
    }
}
