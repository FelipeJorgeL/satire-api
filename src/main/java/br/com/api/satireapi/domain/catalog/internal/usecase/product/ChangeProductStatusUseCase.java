package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import br.com.api.satireapi.domain.catalog.internal.persistence.ProductRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeProductStatusUseCase {

    private final ProductRepository productRepository;

    public ChangeProductStatusUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(UUID productId, boolean active) {
        var product = productRepository.findByIdForUpdate(productId)
            .orElseThrow(ProductNotFoundException::new);
        product.changeStatus(active);
    }
}
