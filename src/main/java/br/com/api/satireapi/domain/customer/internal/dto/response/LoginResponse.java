package br.com.api.satireapi.domain.customer.internal.dto.response;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
