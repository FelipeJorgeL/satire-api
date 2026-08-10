package br.com.api.satireapi.domain.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
    @NotNull UUID categoryId,
    @NotBlank @Size(min = 2, max = 180) String name,
    @NotBlank @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*") @Size(max = 200) String slug,
    @Size(max = 10000) String description,
    @NotNull @Size(max = 100) List<@Valid ProductVariationRequest> variations,
    @NotNull @Size(max = 20) List<@Valid ProductImageRequest> images
) {

    @AssertTrue(message = "A lista de variações não pode conter SKUs duplicados")
    public boolean hasUniqueVariationSkus() {
        if (variations == null) {
            return true;
        }
        return variations.stream().map(ProductVariationRequest::sku).map(String::trim).map(String::toLowerCase)
            .collect(java.util.stream.Collectors.toSet()).size() == variations.size();
    }

    @AssertTrue(message = "As imagens devem ter uma única imagem principal e ordens distintas")
    public boolean hasValidImages() {
        if (images == null || images.isEmpty()) {
            return true;
        }
        var primaryCount = images.stream().filter(ProductImageRequest::primary).count();
        var orders = new HashSet<Integer>();
        return primaryCount == 1 && images.stream().allMatch(image -> orders.add(image.displayOrder()));
    }
}
