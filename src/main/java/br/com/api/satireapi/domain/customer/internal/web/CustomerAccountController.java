package br.com.api.satireapi.domain.customer.internal.web;

import br.com.api.satireapi.domain.customer.internal.dto.request.CreateCustomerAddressRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateCustomerAccountRequest;
import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateCustomerAddressRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerAddressResponse;
import br.com.api.satireapi.domain.customer.internal.usecase.account.DeactivateCustomerAccountUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.account.UpdateCustomerAccountUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.CreateCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.DeleteCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.GetCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.ListCustomerAddressesUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.SetPrimaryCustomerAddressUseCase;
import br.com.api.satireapi.domain.customer.internal.usecase.address.UpdateCustomerAddressUseCase;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/v1/me")
class CustomerAccountController {

    private final UpdateCustomerAccountUseCase updateCustomerAccountUseCase;
    private final DeactivateCustomerAccountUseCase deactivateCustomerAccountUseCase;
    private final ListCustomerAddressesUseCase listCustomerAddressesUseCase;
    private final GetCustomerAddressUseCase getCustomerAddressUseCase;
    private final CreateCustomerAddressUseCase createCustomerAddressUseCase;
    private final UpdateCustomerAddressUseCase updateCustomerAddressUseCase;
    private final DeleteCustomerAddressUseCase deleteCustomerAddressUseCase;
    private final SetPrimaryCustomerAddressUseCase setPrimaryCustomerAddressUseCase;

    CustomerAccountController(
        UpdateCustomerAccountUseCase updateCustomerAccountUseCase,
        DeactivateCustomerAccountUseCase deactivateCustomerAccountUseCase,
        ListCustomerAddressesUseCase listCustomerAddressesUseCase,
        GetCustomerAddressUseCase getCustomerAddressUseCase,
        CreateCustomerAddressUseCase createCustomerAddressUseCase,
        UpdateCustomerAddressUseCase updateCustomerAddressUseCase,
        DeleteCustomerAddressUseCase deleteCustomerAddressUseCase,
        SetPrimaryCustomerAddressUseCase setPrimaryCustomerAddressUseCase
    ) {
        this.updateCustomerAccountUseCase = updateCustomerAccountUseCase;
        this.deactivateCustomerAccountUseCase = deactivateCustomerAccountUseCase;
        this.listCustomerAddressesUseCase = listCustomerAddressesUseCase;
        this.getCustomerAddressUseCase = getCustomerAddressUseCase;
        this.createCustomerAddressUseCase = createCustomerAddressUseCase;
        this.updateCustomerAddressUseCase = updateCustomerAddressUseCase;
        this.deleteCustomerAddressUseCase = deleteCustomerAddressUseCase;
        this.setPrimaryCustomerAddressUseCase = setPrimaryCustomerAddressUseCase;
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateAccount(
        @AuthenticationPrincipal UUID customerId,
        @Valid @RequestBody UpdateCustomerAccountRequest request
    ) {
        updateCustomerAccountUseCase.execute(customerId, request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivateAccount(@AuthenticationPrincipal UUID customerId) {
        deactivateCustomerAccountUseCase.execute(customerId);
    }

    @GetMapping("/addresses")
    List<CustomerAddressResponse> listAddresses(@AuthenticationPrincipal UUID customerId) {
        return listCustomerAddressesUseCase.execute(customerId);
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    CustomerAddressResponse createAddress(
        @AuthenticationPrincipal UUID customerId,
        @Valid @RequestBody CreateCustomerAddressRequest request
    ) {
        return createCustomerAddressUseCase.execute(customerId, request);
    }

    @GetMapping("/addresses/{addressId}")
    CustomerAddressResponse getAddress(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID addressId
    ) {
        return getCustomerAddressUseCase.execute(customerId, addressId);
    }

    @PatchMapping("/addresses/{addressId}")
    CustomerAddressResponse updateAddress(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID addressId,
        @Valid @RequestBody UpdateCustomerAddressRequest request
    ) {
        return updateCustomerAddressUseCase.execute(customerId, addressId, request);
    }

    @DeleteMapping("/addresses/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAddress(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID addressId
    ) {
        deleteCustomerAddressUseCase.execute(customerId, addressId);
    }

    @PatchMapping("/addresses/{addressId}/primary")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void setPrimaryAddress(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID addressId
    ) {
        setPrimaryCustomerAddressUseCase.execute(customerId, addressId);
    }
}
