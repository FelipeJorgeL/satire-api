package br.com.api.satireapi.domain.customer.internal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "perfis")
public class Profile {

    @Id
    private UUID id;

    @Column(name = "nome", nullable = false, unique = true, length = 30)
    private String name;

    protected Profile() {
    }

    public String getName() {
        return name;
    }
}
