package br.com.api.satireapi.domain.customer.internal.usecase;

import br.com.api.satireapi.domain.customer.PasswordHasher;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.dto.response.CustomerResponse;
import br.com.api.satireapi.domain.customer.internal.mapper.CustomerMapper;
import br.com.api.satireapi.domain.customer.internal.model.Customer;

public class RegisterCustomerUseCase {

    private static final String CUSTOMER_PROFILE = "CLIENTE";

    private final CustomerRegistry customerRegistry;
    private final ProfileFinder profileFinder;
    private final PasswordHasher passwordHasher;
    private final EmailConfirmationStore emailConfirmationStore;
    private final OpaqueTokenGenerator tokenGenerator;
    private final ConfirmationEmailOutboxStore outboxStore;
    private final ConfirmationLinkProtector linkProtector;
    private final String baseUrl;
    private final long confirmationExpirationSeconds;

    public RegisterCustomerUseCase(
        CustomerRegistry customerRegistry,
        ProfileFinder profileFinder,
        PasswordHasher passwordHasher,
        EmailConfirmationStore emailConfirmationStore,
        OpaqueTokenGenerator tokenGenerator,
        ConfirmationEmailOutboxStore outboxStore,
        ConfirmationLinkProtector linkProtector,
        @Value("${app.base-url}") String baseUrl,
        @Value("${app.mail.confirmation-expiration}") long confirmationExpirationSeconds
    ) {
        this.customerRegistry = customerRegistry;
        this.profileFinder = profileFinder;
        this.passwordHasher = passwordHasher;
        this.emailConfirmationStore = emailConfirmationStore;
        this.tokenGenerator = tokenGenerator;
        this.outboxStore = outboxStore;
        this.linkProtector = linkProtector;
        this.baseUrl = baseUrl;
        this.confirmationExpirationSeconds = confirmationExpirationSeconds;
    }

    @Transactional
    public CustomerResponse execute(RegisterCustomerRequest request) {
        var normalizedEmail = Customer.normalizeEmail(request.email());
        // Hash antes da checagem de duplicidade: os dois caminhos pagam o custo de BCrypt,
        // impedindo enumeração de e-mails pela diferença de tempo de resposta.
        var passwordHash = passwordHasher.encode(request.password());
        if (customerRegistry.existsByEmail(normalizedEmail)) {
            throw new CustomerEmailAlreadyExistsException();
        }

        var profile = profileFinder.findByName(CUSTOMER_PROFILE)
            .orElseThrow(CustomerProfileNotConfiguredException::new);
        var customer = Customer.register(
            request.name(),
            normalizedEmail,
            passwordHash,
            request.cpf(),
            request.phone()
        );
        customer.assignProfile(profile);
        var saved = customerRegistry.save(customer);

        var rawToken = tokenGenerator.generate();
        var expiresAt = Instant.now().plusSeconds(confirmationExpirationSeconds);
        emailConfirmationStore.save(saved.getId(), tokenGenerator.hash(rawToken), expiresAt);

        var confirmationLink = baseUrl + "/api/v1/auth/confirm?token=" + rawToken;
        outboxStore.enqueue(saved.getId(), saved.getEmail(), linkProtector.protect(confirmationLink));

        return CustomerMapper.toResponse(saved);
    }
}
