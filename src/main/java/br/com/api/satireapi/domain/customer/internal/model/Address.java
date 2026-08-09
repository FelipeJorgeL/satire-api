package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "enderecos")
public class Address {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID customerId;

    @Column(name = "apelido", length = 50)
    private String label;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String recipient;

    @Column(name = "cep", nullable = false, columnDefinition = "char(8)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String postalCode;

    @Column(name = "rua", nullable = false, length = 180)
    private String street;

    @Column(name = "numero", nullable = false, length = 20)
    private String number;

    @Column(name = "complemento", length = 120)
    private String complement;

    @Column(name = "bairro", nullable = false, length = 100)
    private String neighborhood;

    @Column(name = "cidade", nullable = false, length = 100)
    private String city;

    @Column(name = "uf", nullable = false, columnDefinition = "char(2)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String state;

    @Column(name = "principal", nullable = false)
    private boolean primary;

    @Column(name = "criado_em", nullable = false)
    private OffsetDateTime createdAt;

    protected Address() {
    }

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getNumber() {
        return number;
    }

    public String getComplement() {
        return complement;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public boolean isPrimary() {
        return primary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
