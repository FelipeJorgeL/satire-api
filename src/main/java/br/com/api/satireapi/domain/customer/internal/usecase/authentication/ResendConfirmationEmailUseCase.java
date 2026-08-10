package br.com.api.satireapi.domain.customer.internal.usecase.authentication;

import br.com.api.satireapi.domain.customer.ConfirmationEmailOutboxStore;
import br.com.api.satireapi.domain.customer.ConfirmationLinkProtector;
import br.com.api.satireapi.domain.customer.internal.dto.request.ResendConfirmationRequest;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.usecase.CustomerFinder;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResendConfirmationEmailUseCase {

    private final CustomerFinder customerFinder;
    private final EmailConfirmationStore emailConfirmationStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final ConfirmationEmailOutboxStore outboxStore;
    private final ConfirmationLinkProtector linkProtector;
    private final String baseUrl;
    private final long confirmationExpirationSeconds;

    public ResendConfirmationEmailUseCase(
        CustomerFinder customerFinder,
        EmailConfirmationStore emailConfirmationStore,
        OpaqueTokenGenerator tokenGenerator,
        ConfirmationEmailOutboxStore outboxStore,
        ConfirmationLinkProtector linkProtector,
        @Value("${app.base-url}") String baseUrl,
        @Value("${app.mail.confirmation-expiration}") long confirmationExpirationSeconds
    ) {
        this.customerFinder = customerFinder;
        this.emailConfirmationStore = emailConfirmationStore;
        this.tokenGenerator = tokenGenerator;
        this.outboxStore = outboxStore;
        this.linkProtector = linkProtector;
        this.baseUrl = baseUrl;
        this.confirmationExpirationSeconds = confirmationExpirationSeconds;
    }

    @Transactional
    public void execute(ResendConfirmationRequest request) {
        customerFinder.findByEmail(Customer.normalizeEmail(request.email()))
            .filter(customer -> !customer.isActive())
            .ifPresent(this::queueConfirmation);
    }

    private void queueConfirmation(Customer customer) {
        var rawToken = tokenGenerator.generate();
        var expiresAt = Instant.now().plusSeconds(confirmationExpirationSeconds);
        emailConfirmationStore.save(customer.getId(), tokenGenerator.hash(rawToken), expiresAt);
        var link = baseUrl + "/api/v1/auth/confirm?token=" + rawToken;
        outboxStore.enqueue(customer.getId(), customer.getEmail(), linkProtector.protect(link));
    }
}
