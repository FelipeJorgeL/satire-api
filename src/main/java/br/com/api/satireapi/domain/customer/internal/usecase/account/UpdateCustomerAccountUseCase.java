package br.com.api.satireapi.domain.customer.internal.usecase.account;

import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateCustomerAccountRequest;
import br.com.api.satireapi.domain.customer.internal.persistence.CustomerRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCustomerAccountUseCase {

    private final CustomerRepository customerRepository;

    public UpdateCustomerAccountUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void execute(UUID customerId, UpdateCustomerAccountRequest request) {
        var customer = customerRepository.findByIdForUpdate(customerId)
            .filter(existingCustomer -> existingCustomer.isActive())
            .orElseThrow(CustomerAccountNotFoundException::new);
        customer.updateAdministrativeData(request.name(), null, null, request.phone());
    }
}
