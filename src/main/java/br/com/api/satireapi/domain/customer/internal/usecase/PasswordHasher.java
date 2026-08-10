package br.com.api.satireapi.domain.customer.internal.usecase;

public interface PasswordHasher {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
