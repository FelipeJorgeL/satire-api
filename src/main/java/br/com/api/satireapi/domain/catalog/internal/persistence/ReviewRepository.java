package br.com.api.satireapi.domain.catalog.internal.persistence;

import br.com.api.satireapi.domain.catalog.internal.model.Review;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findAllByProductId(UUID productId, Pageable pageable);

    Optional<Review> findByCustomerIdAndProductId(UUID customerId, UUID productId);

    @Modifying
    @Query(value = """
        INSERT INTO avaliacoes
            (id, usuario_id, produto_id, pedido_id, nota, comentario, criado_em, atualizado_em)
        VALUES
            (:reviewId, :customerId, :productId, :orderId, :rating, :comment,
             CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
        ON CONFLICT (usuario_id, produto_id) DO UPDATE
        SET pedido_id = EXCLUDED.pedido_id,
            nota = EXCLUDED.nota,
            comentario = EXCLUDED.comentario
        WHERE (avaliacoes.pedido_id, avaliacoes.nota, avaliacoes.comentario)
            IS DISTINCT FROM (EXCLUDED.pedido_id, EXCLUDED.nota, EXCLUDED.comentario)
        """, nativeQuery = true)
    int upsert(
        @Param("reviewId") UUID reviewId,
        @Param("customerId") UUID customerId,
        @Param("productId") UUID productId,
        @Param("orderId") UUID orderId,
        @Param("rating") short rating,
        @Param("comment") String comment
    );

    @Modifying
    @Query("""
        delete from Review review
        where review.customerId = :customerId and review.productId = :productId
        """)
    int deleteOwned(
        @Param("customerId") UUID customerId,
        @Param("productId") UUID productId
    );
}
