package br.com.api.satireapi.domain.customer.internal.usecase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class LogoutUseCaseTest {

    private final RefreshTokenStore refreshTokenStore = mock(RefreshTokenStore.class);
    private final LogoutUseCase useCase = new LogoutUseCase(refreshTokenStore);

    @Test
    void deletesRefreshTokenForCustomer() {
        var customerId = UUID.randomUUID();

        useCase.execute(customerId);

        verify(refreshTokenStore).deleteByCustomerId(customerId);
    }
}
