package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.persistence.ProductImageRepository;
import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProductImageUseCase {

    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;

    public DeleteProductImageUseCase(
        ProductRepository productRepository,
        ProductImageRepository imageRepository
    ) {
        this.productRepository = productRepository;
        this.imageRepository = imageRepository;
    }

    @Transactional
    public void execute(UUID productId, UUID imageId) {
        productRepository.findByIdForUpdate(productId)
            .orElseThrow(ProductNotFoundException::new);
        var image = imageRepository.findByIdAndProductId(imageId, productId)
            .orElseThrow(ProductImageNotFoundException::new);
        imageRepository.delete(image);
    }
}
