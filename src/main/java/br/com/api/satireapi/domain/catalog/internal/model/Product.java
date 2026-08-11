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
@Table(name = "produtos")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoryId;

    @Column(name = "nome", nullable = false, length = 180)
    private String name;

    @Column(name = "slug", nullable = false, length = 200, unique = true)
    private String slug;

    @Column(name = "descricao")
    private String description;

    @Column(name = "ativo", nullable = false)
    private boolean active;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "excluido_em")
    private OffsetDateTime deletedAt;

    protected Product() {
    }

    private Product(UUID categoryId, String name, String slug, String description) {
        this.categoryId = categoryId;
        this.name = name.trim();
        this.slug = slug.trim();
        this.description = blankToNull(description);
        this.active = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Product create(UUID categoryId, String name, String slug, String description) {
        return new Product(categoryId, name, slug, description);
    }

    public void replace(UUID categoryId, String name, String slug, String description) {
        this.categoryId = categoryId;
        this.name = name.trim();
        this.slug = slug.trim();
        this.description = blankToNull(description);
        touch();
    }

    public void changeStatus(boolean active) {
        if (this.active == active) {
            return;
        }
        this.active = active;
        this.deletedAt = active ? null : OffsetDateTime.now();
        touch();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}
