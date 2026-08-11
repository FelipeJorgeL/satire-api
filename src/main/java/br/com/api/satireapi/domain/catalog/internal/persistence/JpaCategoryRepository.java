package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByActiveTrue();
    Optional<Category> findByIdAndActiveTrue(UUID id);
}
