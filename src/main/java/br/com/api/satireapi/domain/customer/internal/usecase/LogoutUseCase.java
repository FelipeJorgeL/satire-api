package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public class LogoutUseCase {

    private final RefreshTokenStore refreshTokenStore;

    public LogoutUseCase(RefreshTokenStore refreshTokenStore) {
        this.refreshTokenStore = refreshTokenStore;
    }

    @Transactional
    public void execute(UUID customerId) {
        refreshTokenStore.deleteByCustomerId(customerId);
    }
}
