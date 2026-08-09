package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssignCustomerProfileUseCase {

    private final AdminCustomerQuery adminCustomerQuery;

    public AssignCustomerProfileUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional
    public void execute(UUID customerId, String profileName) {
        var customer = adminCustomerQuery.findByIdForUpdate(customerId)
            .orElseThrow(AdminCustomerNotFoundException::new);
        var profile = adminCustomerQuery.findProfileByName(profileName)
            .orElseThrow(CustomerProfileNotConfiguredException::new);
        customer.assignProfile(profile);
    }
}
