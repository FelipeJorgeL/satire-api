package br.com.api.satireapi.domain.customer.internal.web;

import br.com.api.satireapi.domain.customer.internal.dto.request.AdminCustomerFilter;
import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateAdminCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateAdminCustomerStatusRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerDetailsResponse;
import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerListItemResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.AssignCustomerProfileUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.ChangeAdminCustomerStatusUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.GetAdminCustomerUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.ListAdminCustomersUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.RemoveCustomerProfileUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.UpdateAdminCustomerUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminCustomerController {

    private final ListAdminCustomersUseCase listAdminCustomersUseCase;
    private final GetAdminCustomerUseCase getAdminCustomerUseCase;
    private final UpdateAdminCustomerUseCase updateAdminCustomerUseCase;
    private final ChangeAdminCustomerStatusUseCase changeAdminCustomerStatusUseCase;
    private final AssignCustomerProfileUseCase assignCustomerProfileUseCase;
    private final RemoveCustomerProfileUseCase removeCustomerProfileUseCase;

    AdminCustomerController(
        ListAdminCustomersUseCase listAdminCustomersUseCase,
        GetAdminCustomerUseCase getAdminCustomerUseCase,
        UpdateAdminCustomerUseCase updateAdminCustomerUseCase,
        ChangeAdminCustomerStatusUseCase changeAdminCustomerStatusUseCase,
        AssignCustomerProfileUseCase assignCustomerProfileUseCase,
        RemoveCustomerProfileUseCase removeCustomerProfileUseCase
    ) {
        this.listAdminCustomersUseCase = listAdminCustomersUseCase;
        this.getAdminCustomerUseCase = getAdminCustomerUseCase;
        this.updateAdminCustomerUseCase = updateAdminCustomerUseCase;
        this.changeAdminCustomerStatusUseCase = changeAdminCustomerStatusUseCase;
        this.assignCustomerProfileUseCase = assignCustomerProfileUseCase;
        this.removeCustomerProfileUseCase = removeCustomerProfileUseCase;
    }

    @GetMapping
    PagedModel<AdminCustomerListItemResponse> list(
        @RequestParam(required = false) @Size(max = 100) String search,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) @Pattern(regexp = "CLIENTE|ADMIN") String profile,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "name|email|active|createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return new PagedModel<>(
            listAdminCustomersUseCase.execute(new AdminCustomerFilter(search, active, profile), pageable)
        );
    }

    @GetMapping("/{userId}")
    AdminCustomerDetailsResponse findById(@PathVariable UUID userId) {
        return getAdminCustomerUseCase.execute(userId);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void update(@PathVariable UUID userId, @Valid @RequestBody UpdateAdminCustomerRequest request) {
        updateAdminCustomerUseCase.execute(userId, request);
    }

    @PatchMapping("/{userId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeStatus(@PathVariable UUID userId, @Valid @RequestBody UpdateAdminCustomerStatusRequest request) {
        changeAdminCustomerStatusUseCase.execute(userId, request.active());
    }

    @PutMapping("/{userId}/profiles/{profile}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void assignProfile(
        @PathVariable UUID userId,
        @PathVariable @Pattern(regexp = "CLIENTE|ADMIN") String profile
    ) {
        assignCustomerProfileUseCase.execute(userId, profile);
    }

    @DeleteMapping("/{userId}/profiles/{profile}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void removeProfile(
        @PathVariable UUID userId,
        @PathVariable @Pattern(regexp = "CLIENTE|ADMIN") String profile
    ) {
        removeCustomerProfileUseCase.execute(userId, profile);
    }
}
