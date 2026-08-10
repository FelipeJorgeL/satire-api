package br.com.api.satireapi.domain.order.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "enderecos_pedidos")
public class OrderAddressSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "destinatario", nullable = false, length = 120)
    private String recipient;

    @Column(name = "cep", nullable = false, length = 8, columnDefinition = "char(8)")
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

    @Column(name = "uf", nullable = false, length = 2, columnDefinition = "char(2)")
    @JdbcTypeCode(SqlTypes.CHAR)
    private String state;

    protected OrderAddressSnapshot() {
    }

    private OrderAddressSnapshot(
        UUID orderId,
        String recipient,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
    ) {
        this.orderId = orderId;
        this.recipient = requireText(recipient);
        this.postalCode = requireText(postalCode);
        this.street = requireText(street);
        this.number = requireText(number);
        this.complement = blankToNull(complement);
        this.neighborhood = requireText(neighborhood);
        this.city = requireText(city);
        this.state = requireText(state).toUpperCase(java.util.Locale.ROOT);
    }

    public static OrderAddressSnapshot create(
        UUID orderId,
        String recipient,
        String postalCode,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state
    ) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order is required for address snapshot");
        }
        return new OrderAddressSnapshot(
            orderId, recipient, postalCode, street, number, complement,
            neighborhood, city, state
        );
    }

    private static String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Address field is required");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
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
}
