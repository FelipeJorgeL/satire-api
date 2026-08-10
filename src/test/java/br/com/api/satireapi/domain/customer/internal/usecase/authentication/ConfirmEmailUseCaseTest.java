package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerFinder;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerRegistry;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConfirmEmailUseCaseTest {

    private final EmailConfirmationStore emailConfirmationStore = mock(EmailConfirmationStore.class);
    private final OpaqueTokenGenerator tokenGenerator = new OpaqueTokenGenerator();
    private final CustomerFinder customerFinder = mock(CustomerFinder.class);
    private final CustomerRegistry customerRegistry = mock(CustomerRegistry.class);
    private final ConfirmEmailUseCase useCase = new ConfirmEmailUseCase(
        emailConfirmationStore, tokenGenerator, customerFinder, customerRegistry
    );

    @Test
    void activatesCustomerAndConsumesTokenForValidToken() {
        var customerId = UUID.randomUUID();
        var rawToken = "valid-confirmation-token";
        var tokenHash = tokenGenerator.hash(rawToken);
        var customer = mock(Customer.class);
        when(emailConfirmationStore.findCustomerIdByHash(eq(tokenHash), any(Instant.class)))
            .thenReturn(Optional.of(customerId));
        when(emailConfirmationStore.consume(eq(customerId), eq(tokenHash), any(Instant.class)))
            .thenReturn(true);
        when(customerFinder.findById(customerId)).thenReturn(Optional.of(customer));

        useCase.execute(rawToken);

        verify(customer).activate();
        verify(customerRegistry).save(customer);
    }

    @Test
    void rejectsSecondUseWhenAtomicConsumptionLosesTheRace() {
        var customerId = UUID.randomUUID();
        var rawToken = "already-consumed-token";
        var tokenHash = tokenGenerator.hash(rawToken);
        when(emailConfirmationStore.findCustomerIdByHash(eq(tokenHash), any(Instant.class)))
            .thenReturn(Optional.of(customerId));
        when(emailConfirmationStore.consume(eq(customerId), eq(tokenHash), any(Instant.class)))
            .thenReturn(false);

        assertThrows(
            InvalidEmailConfirmationTokenException.class,
            () -> useCase.execute(rawToken)
        );
        verify(customerRegistry, never()).save(any());
    }
}
