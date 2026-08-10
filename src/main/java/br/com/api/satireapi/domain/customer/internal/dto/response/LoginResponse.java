package br.com.api.satireapi.domain.customer.internal.dto.response;

import java.util.List;

public record LoginResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String refreshToken,
    long refreshExpiresIn,
    List<String> profiles
) {

    public LoginResponse(String accessToken, String tokenType, long expiresIn) {
        this(accessToken, tokenType, expiresIn, null, 0, List.of());
    }

    public LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        long refreshExpiresIn
    ) {
        this(accessToken, tokenType, expiresIn, refreshToken, refreshExpiresIn, List.of());
    }

    public LoginResponse {
        profiles = List.copyOf(profiles == null ? List.of() : profiles);
    }
}
