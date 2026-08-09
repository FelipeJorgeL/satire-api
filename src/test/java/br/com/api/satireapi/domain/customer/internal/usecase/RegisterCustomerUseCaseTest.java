package br.com.api.satireapi.domain.customer.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.api.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.api.satireapi.domain.customer.internal.model.Customer;
import br.com.api.satireapi.domain.customer.internal.model.Profile;
import br.com.api.satireapi.infra.mail.SendGridEmailSender;

class RegisterCustomerUseCaseTest {

    private final CustomerRegistry customerRegistry = mock(CustomerRegistry.class);
    private final ProfileFinder profileFinder = mock(ProfileFinder.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final EmailConfirmationStore emailConfirmationStore = mock(EmailConfirmationStore.class);
    private final OpaqueTokenGenerator tokenGenerator = new OpaqueTokenGenerator();
    private final SendGridEmailSender emailSender = mock(SendGridEmailSender.class);
    private final RegisterCustomerUseCase useCase = new RegisterCustomerUseCase(
        customerRegistry,
        profileFinder,
        passwordEncoder,
        emailConfirmationStore,
        tokenGenerator,
        emailSender,
        "http://localhost:8080",
        86_400L
    );

    @Test
    void registersCustomerWithNormalizedEmailAndEncryptedPassword() {
        var request = new RegisterCustomerRequest(
            "Felipe Jorge",
            "  FELIPE@EXAMPLE.COM ",
            "safe-password",
            "12345678901",
            "11999999999"
        );
        var profile = mock(Profile.class);

        when(customerRegistry.existsByEmail("felipe@example.com")).thenReturn(false);
        when(profileFinder.findByName("CLIENTE")).thenReturn(Optional.of(profile));
        when(profile.getName()).thenReturn("CLIENTE");
        when(passwordEncoder.encode("safe-password")).thenReturn("encoded-password");
        when(customerRegistry.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(request);

        assertEquals("Felipe Jorge", response.name());
        assertEquals("felipe@example.com", response.email());
        verify(passwordEncoder).encode("safe-password");
        verify(customerRegistry).save(any(Customer.class));
    }

    @Test
    void rejectsDuplicatedNormalizedEmail() {
        var request = new RegisterCustomerRequest(
            "Felipe Jorge",
            " FELIPE@EXAMPLE.COM ",
            "safe-password",
            null,
            null
        );

        when(customerRegistry.existsByEmail("felipe@example.com")).thenReturn(true);

        assertThrows(CustomerEmailAlreadyExistsException.class, () -> useCase.execute(request));
        verify(customerRegistry, never()).save(any());
    }
}
