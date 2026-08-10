package br.com.api.satireapi.domain.catalog.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "categorias")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "ativo", nullable = false)
    private boolean active;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "excluido_em")
    private OffsetDateTime deletedAt;

    protected Category() {
    }

    private Category(String name, String slug) {
        this.name = name.trim();
        this.slug = slug.trim();
        this.active = true;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Category create(String name, String slug) {
        return new Category(name, slug);
    }

    public void update(String name, String slug) {
        this.name = name == null ? this.name : name.trim();
        this.slug = slug == null ? this.slug : slug.trim();
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

    public void setId(UUID id) {
        this.id = id;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
