package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.internal.dto.request.AdminProductFilter;
import br.com.api.satireapi.domain.catalog.internal.dto.request.ProductRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.request.UpdateProductStatusRequest;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.AdminProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ChangeProductStatusUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.CreateProductUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.DeleteProductImageUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ListAdminProductsUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.product.ReplaceProductUseCase;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/products")
class AdminProductController {

    private final ListAdminProductsUseCase listAdminProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final ReplaceProductUseCase replaceProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final DeleteProductImageUseCase deleteProductImageUseCase;

    AdminProductController(
        ListAdminProductsUseCase listAdminProductsUseCase,
        CreateProductUseCase createProductUseCase,
        ReplaceProductUseCase replaceProductUseCase,
        ChangeProductStatusUseCase changeProductStatusUseCase,
        DeleteProductImageUseCase deleteProductImageUseCase
    ) {
        this.listAdminProductsUseCase = listAdminProductsUseCase;
        this.createProductUseCase = createProductUseCase;
        this.replaceProductUseCase = replaceProductUseCase;
        this.changeProductStatusUseCase = changeProductStatusUseCase;
        this.deleteProductImageUseCase = deleteProductImageUseCase;
    }

    @GetMapping
    PagedModel<AdminProductListItemResponse> list(
        @RequestParam(required = false) @Size(max = 100) String search,
        @RequestParam(required = false) Boolean active,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "name|slug|active|createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return new PagedModel<>(listAdminProductsUseCase.execute(
            new AdminProductFilter(search, active, categoryId), pageable
        ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdminProductDetailsResponse create(@Valid @RequestBody ProductRequest request) {
        return createProductUseCase.execute(request);
    }

    @PutMapping("/{productId}")
    AdminProductDetailsResponse replace(
        @PathVariable UUID productId,
        @Valid @RequestBody ProductRequest request
    ) {
        return replaceProductUseCase.execute(productId, request);
    }

    @PatchMapping("/{productId}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeStatus(
        @PathVariable UUID productId,
        @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        changeProductStatusUseCase.execute(productId, request.active());
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@PathVariable UUID productId) {
        changeProductStatusUseCase.execute(productId, false);
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteImage(@PathVariable UUID productId, @PathVariable UUID imageId) {
        deleteProductImageUseCase.execute(productId, imageId);
    }
}
