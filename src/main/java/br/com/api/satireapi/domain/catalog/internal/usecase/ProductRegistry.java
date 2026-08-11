package br.com.api.satireapi.domain.catalog.internal.usecase;

import java.util.UUID;

import br.com.api.satireapi.domain.catalog.internal.model.Product;

public interface ProductRegistry {

    Product save(Product product);

    void delete(Product product);
}