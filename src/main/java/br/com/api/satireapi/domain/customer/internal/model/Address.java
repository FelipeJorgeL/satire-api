package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "enderecos")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    private Address(
        UUID customerId,
        String label,
        String recipient,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        boolean primary
    ) {
        this.customerId = customerId;
        this.label = blankToNull(label);
        this.recipient = recipient.trim();
        this.postalCode = postalCode.trim();
        this.street = street.trim();
        this.number = number.trim();
        this.complement = blankToNull(complement);
        this.neighborhood = neighborhood.trim();
        this.city = city.trim();
        this.state = state.trim().toUpperCase(java.util.Locale.ROOT);
        this.primary = primary;
        this.createdAt = OffsetDateTime.now();
    }

    public static Address create(
        UUID customerId,
        String label,
        String recipient,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        boolean primary
    ) {
        return new Address(
            customerId, label, recipient, postalCode, street, number,
            complement, neighborhood, city, state, primary
        );
    }

    public void update(
        String label,
        String recipient,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
    ) {
        this.label = blankToNull(label);
        this.recipient = recipient.trim();
        this.postalCode = postalCode.trim();
        this.street = street.trim();
        this.number = number.trim();
        this.complement = blankToNull(complement);
        this.neighborhood = neighborhood.trim();
        this.city = city.trim();
        this.state = state.trim().toUpperCase(java.util.Locale.ROOT);
    }

    public void markPrimary(boolean primary) {
        this.primary = primary;
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

    public UUID getCustomerId() {
        return customerId;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
