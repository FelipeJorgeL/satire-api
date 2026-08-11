package br.com.api.satireapi.domain.catalog.internal.web;

import br.com.api.satireapi.domain.catalog.dto.ProductImageSummary;
import br.com.api.satireapi.domain.catalog.dto.ProductVariationSummary;
import br.com.api.satireapi.domain.catalog.internal.dto.request.PublicProductFilter;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductDetailsResponse;
import br.com.api.satireapi.domain.catalog.internal.dto.response.PublicProductListItemResponse;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.GetPublicProductUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductImagesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductsUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.query.ListPublicProductVariationsUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
class ProductController {
    private final ListPublicProductsUseCase listProductsUseCase;
    private final GetPublicProductUseCase getProductUseCase;
    private final ListPublicProductVariationsUseCase listVariationsUseCase;
    private final ListPublicProductImagesUseCase listImagesUseCase;

    ProductController(
        ListPublicProductsUseCase listProductsUseCase,
        GetPublicProductUseCase getProductUseCase,
        ListPublicProductVariationsUseCase listVariationsUseCase,
        ListPublicProductImagesUseCase listImagesUseCase
    ) {
        this.listProductsUseCase = listProductsUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listVariationsUseCase = listVariationsUseCase;
        this.listImagesUseCase = listImagesUseCase;
    }

    @GetMapping
    PagedModel<PublicProductListItemResponse> list(
        @RequestParam(required = false) @Size(max = 100) String search,
        @RequestParam(required = false) UUID categoryId,
        @RequestParam(defaultValue = "0") @Min(0) int page,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
        @RequestParam(defaultValue = "createdAt")
        @Pattern(regexp = "name|createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") @Pattern(regexp = "(?i)asc|desc") String direction
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        return new PagedModel<>(listProductsUseCase.execute(
            new PublicProductFilter(search, categoryId), pageable
        ));
    }

    @GetMapping("/{productId}")
    PublicProductDetailsResponse get(@PathVariable UUID productId) {
        return getProductUseCase.byId(productId);
    }

    @GetMapping("/slug/{slug}")
    PublicProductDetailsResponse getBySlug(
        @PathVariable @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 200) String slug
    ) {
        return getProductUseCase.bySlug(slug);
    }

    @GetMapping("/{productId}/variations")
    List<ProductVariationSummary> listVariations(@PathVariable UUID productId) {
        return listVariationsUseCase.execute(productId);
    }

    @GetMapping("/{productId}/images")
    List<ProductImageSummary> listImages(@PathVariable UUID productId) {
        return listImagesUseCase.execute(productId);
    }
}
