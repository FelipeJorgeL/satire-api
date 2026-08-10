package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ProductSlugAlreadyExistsException extends RuntimeException {

    public ProductSlugAlreadyExistsException() {
        super("Slug de produto já existe");
    }
}
