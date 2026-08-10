package br.com.api.satireapi.domain.catalog.internal.usecase.category;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CategorySlugAlreadyExistsException extends RuntimeException {

    public CategorySlugAlreadyExistsException() {
        super("Slug de categoria já existe");
    }
}
