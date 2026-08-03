package br.com.felipejorge.satireapi.domain.customer.internal.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.felipejorge.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.felipejorge.satireapi.domain.customer.internal.model.Customer;
import br.com.felipejorge.satireapi.domain.customer.internal.model.Profile;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class RegisterCustomerUseCaseTest {

    private final CustomerRegistry customerRegistry = mock(CustomerRegistry.class);
    private final ProfileFinder profileFinder = mock(ProfileFinder.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final RegisterCustomerUseCase useCase = new RegisterCustomerUseCase(
        customerRegistry,
        profileFinder,
        passwordEncoder
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
