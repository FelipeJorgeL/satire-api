package br.com.api.satireapi.domain.catalog.internal.usecase.product;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductImageNotFoundException extends RuntimeException {

    public ProductImageNotFoundException() {
        super("Imagem do produto não encontrada");
    }
}
