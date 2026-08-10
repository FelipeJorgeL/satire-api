package br.com.api.satireapi.domain.catalog.internal.model;

import br.com.api.satireapi.domain.catalog.ProductVariationStockCapacityExceededException;
import br.com.api.satireapi.domain.catalog.ProductVariationStockUnavailableException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "variacoes_produtos")
public class ProductVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "produto_id", nullable = false)
    private UUID productId;

    @Column(name = "sku", nullable = false, unique = true, length = 60)
    private String sku;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "preco", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "estoque", nullable = false)
    private int stock;

    @Column(name = "ativo", nullable = false)
    private boolean active;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "excluido_em")
    private OffsetDateTime deletedAt;

    protected ProductVariation() {
    }

    private ProductVariation(UUID productId, String sku, String name, BigDecimal price, int stock) {
        this.productId = productId;
        this.sku = sku.trim();
        this.name = name.trim();
        this.price = price;
        this.stock = stock;
        this.active = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static ProductVariation create(
        UUID productId,
        String sku,
        String name,
        BigDecimal price,
        int stock
    ) {
        return new ProductVariation(productId, sku, name, price, stock);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public int adjustStock(int delta) {
        if (delta == 0) {
            throw new IllegalArgumentException("O ajuste de estoque não pode ser zero");
        }

        var adjustedStock = (long) stock + delta;
        if (adjustedStock < 0) {
            throw new ProductVariationStockUnavailableException();
        }
        if (adjustedStock > Integer.MAX_VALUE) {
            throw new ProductVariationStockCapacityExceededException();
        }

        stock = (int) adjustedStock;
        updatedAt = OffsetDateTime.now();
        return stock;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }
}
