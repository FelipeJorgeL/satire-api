package br.com.api.satireapi.domain.catalog.internal.usecase;

import br.com.api.satireapi.domain.catalog.internal.model.ProductImage;

import java.util.List;
import java.util.UUID;

public interface ImageFinder {
    List<ProductImage> findActiveByProductId(UUID productId);
}
