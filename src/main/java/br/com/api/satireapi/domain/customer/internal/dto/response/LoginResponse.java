package br.com.api.satireapi.domain.customer.internal.dto.response;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken,
    long refreshExpiresIn
) {

    public LoginResponse(String accessToken, String tokenType, long expiresIn) {
        this(accessToken, tokenType, expiresIn, null, 0);
    }
}
