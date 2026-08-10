package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;

public class ConfirmEmailUseCase {

    private final EmailConfirmationStore emailConfirmationStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final CustomerFinder customerFinder;
    private final CustomerRegistry customerRegistry;

    public ConfirmEmailUseCase(
        EmailConfirmationStore emailConfirmationStore,
        OpaqueTokenGenerator tokenGenerator,
        CustomerFinder customerFinder,
        CustomerRegistry customerRegistry
    ) {
        this.emailConfirmationStore = emailConfirmationStore;
        this.tokenGenerator = tokenGenerator;
        this.customerFinder = customerFinder;
        this.customerRegistry = customerRegistry;
    }

    @Transactional
    public void execute(String rawToken) {
        if (rawToken == null || rawToken.length() > OpaqueTokenGenerator.MAX_TOKEN_LENGTH) {
            throw new InvalidEmailConfirmationTokenException();
        }
        var tokenHash = tokenGenerator.hash(rawToken);
        var customerId = emailConfirmationStore.findCustomerIdByHash(tokenHash, Instant.now())
            .orElseThrow(InvalidEmailConfirmationTokenException::new);

        if (!emailConfirmationStore.consume(customerId, tokenHash, Instant.now())) {
            throw new InvalidEmailConfirmationTokenException();
        }

        var customer = customerFinder.findById(customerId)
            .orElseThrow(InvalidEmailConfirmationTokenException::new);

        customer.activate();
        customerRegistry.save(customer);
    }
}
