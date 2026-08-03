package br.com.felipejorge.satireapi.domain.customer.internal.usecase;

import br.com.felipejorge.satireapi.domain.customer.internal.dto.request.RegisterCustomerRequest;
import br.com.felipejorge.satireapi.domain.customer.internal.dto.response.CustomerResponse;
import br.com.felipejorge.satireapi.domain.customer.internal.mapper.CustomerMapper;
import br.com.felipejorge.satireapi.domain.customer.internal.model.Customer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterCustomerUseCase {

    private static final String CUSTOMER_PROFILE = "CLIENTE";

    private final CustomerRegistry customerRegistry;
    private final ProfileFinder profileFinder;
    private final PasswordEncoder passwordEncoder;

    public RegisterCustomerUseCase(
        CustomerRegistry customerRegistry,
        ProfileFinder profileFinder,
        PasswordEncoder passwordEncoder
    ) {
        this.customerRegistry = customerRegistry;
        this.profileFinder = profileFinder;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public CustomerResponse execute(RegisterCustomerRequest request) {
        var normalizedEmail = Customer.normalizeEmail(request.email());
        if (customerRegistry.existsByEmail(normalizedEmail)) {
            throw new CustomerEmailAlreadyExistsException();
        }

        var profile = profileFinder.findByName(CUSTOMER_PROFILE)
            .orElseThrow(CustomerProfileNotConfiguredException::new);
        var passwordHash = passwordEncoder.encode(request.password());
        var customer = Customer.register(
            request.name(),
            normalizedEmail,
            passwordHash,
            request.cpf(),
            request.phone()
        );
        customer.assignProfile(profile);

        return CustomerMapper.toResponse(customerRegistry.save(customer));
    }
}
