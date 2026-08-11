package br.com.api.satireapi.domain.catalog.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "imagens_produtos")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "produto_id", nullable = false)
    private UUID productId;

    @Column(name = "url", nullable = false, columnDefinition = "text")
    private String url;

    @Column(name = "texto_alternativo", length = 255)
    private String altText;

    @Column(name = "decorativa", nullable = false)
    private boolean decorative;

    @Column(name = "principal", nullable = false)
    private boolean primary;

    @Column(name = "ordem_exibicao", nullable = false)
    private int displayOrder;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    protected ProductImage() {
    }

    private ProductImage(
        UUID productId,
        String url,
        String altText,
        boolean decorative,
        boolean primary,
        int displayOrder
    ) {
        this.productId = productId;
        this.url = url.trim();
        this.altText = decorative ? "" : blankToNull(altText);
        this.decorative = decorative;
        this.primary = primary;
        this.displayOrder = displayOrder;
        this.createdAt = OffsetDateTime.now();
    }

    public static ProductImage create(
        UUID productId,
        String url,
        String altText,
        boolean decorative,
        boolean primary,
        int displayOrder
    ) {
        return new ProductImage(productId, url, altText, decorative, primary, displayOrder);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getUrl() {
        return url;
    }

    public String getAltText() {
        return altText;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isDecorative() {
        return decorative;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
