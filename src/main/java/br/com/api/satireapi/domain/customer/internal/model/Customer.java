package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "usuarios")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "cpf", unique = true, columnDefinition = "char(11)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String cpf;

    @Column(name = "telefone", length = 20)
    private String phone;

    @Column(name = "ativo", nullable = false)
    private boolean active;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "atualizado_em", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "excluido_em")
    private OffsetDateTime deletedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_perfis",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<Profile> profiles = new LinkedHashSet<>();

    protected Customer() {
    }

    private Customer(String name, String email, String passwordHash, String cpf, String phone) {
        this.name = name.trim();
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.cpf = blankToNull(cpf);
        this.phone = blankToNull(phone);
        this.active = false;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public static Customer register(String name, String email, String passwordHash, String cpf, String phone) {
        return new Customer(name, email, passwordHash, cpf, phone);
    }

    public void assignProfile(Profile profile) {
        if (profiles.add(profile)) {
            touch();
        }
    }

    public void removeProfile(Profile profile) {
        if (profiles.remove(profile)) {
            touch();
        }
    }

    public boolean hasProfile(String profileName) {
        return profiles.stream().anyMatch(profile -> profile.getName().equals(profileName));
    }

    public void updateAdministrativeData(String name, String email, String cpf, String phone) {
        if (name != null) {
            this.name = name.trim();
        }
        if (email != null) {
            this.email = normalizeEmail(email);
        }
        if (cpf != null) {
            this.cpf = blankToNull(cpf);
        }
        if (phone != null) {
            this.phone = blankToNull(phone);
        }
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

    public void activate() {
        changeStatus(true);
    }

    public void activate() {
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getCpf() {
        return cpf;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<Profile> getProfiles() {
        return Collections.unmodifiableSet(profiles);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void touch() {
        this.updatedAt = OffsetDateTime.now();
    }
}
