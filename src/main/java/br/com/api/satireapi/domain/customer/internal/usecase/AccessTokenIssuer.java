package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.List;

public interface AccessTokenIssuer {

    String generate(String subject, String email, List<String> profiles);

    long expirationSeconds();
}
