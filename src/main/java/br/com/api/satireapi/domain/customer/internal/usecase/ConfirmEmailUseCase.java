package br.com.api.satireapi.domain.customer.internal.usecase;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
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
        var tokenHash = tokenGenerator.hash(rawToken);
        var customerId = emailConfirmationStore.findCustomerIdByHash(tokenHash, Instant.now())
            .orElseThrow(InvalidEmailConfirmationTokenException::new);

        var customer = customerFinder.findById(customerId)
            .orElseThrow(InvalidEmailConfirmationTokenException::new);

        customer.activate();
        customerRegistry.save(customer);
        emailConfirmationStore.deleteByCustomerId(customerId);
    }
}
