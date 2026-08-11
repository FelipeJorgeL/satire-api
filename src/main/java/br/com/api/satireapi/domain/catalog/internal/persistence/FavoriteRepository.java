package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.Favorite;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("""
        select favorite
        from Favorite favorite, Product product, Category category
        where favorite.customerId = :customerId
          and product.id = favorite.productId
          and product.active = true
          and category.id = product.categoryId
          and category.active = true
        """)
    Page<Favorite> findVisibleByCustomerId(
        @Param("customerId") UUID customerId,
        Pageable pageable
    );

    @Modifying
    @Query(value = """
        INSERT INTO favoritos (id, usuario_id, produto_id, criado_em, atualizado_em)
        VALUES (gen_random_uuid(), :customerId, :productId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (usuario_id, produto_id) DO NOTHING
        """, nativeQuery = true)
    int addIfAbsent(
        @Param("customerId") UUID customerId,
        @Param("productId") UUID productId
    );

    @Modifying
    @Query("""
        delete from Favorite favorite
        where favorite.customerId = :customerId and favorite.productId = :productId
        """)
    int deleteForCustomer(
        @Param("customerId") UUID customerId,
        @Param("productId") UUID productId
    );
}
