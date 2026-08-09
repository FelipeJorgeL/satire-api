package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RemoveCustomerProfileUseCase {

    private static final String ADMIN_PROFILE = "ADMIN";

    private final AdminCustomerQuery adminCustomerQuery;

    public RemoveCustomerProfileUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional
    public void execute(UUID customerId, String profileName) {
        var customer = adminCustomerQuery.findByIdForUpdate(customerId)
            .orElseThrow(AdminCustomerNotFoundException::new);
        var profile = adminCustomerQuery.findProfileByNameForUpdate(profileName)
            .orElseThrow(CustomerProfileNotConfiguredException::new);

        if (!customer.hasProfile(profileName)) {
            return;
        }
        if (ADMIN_PROFILE.equals(profileName) && adminCustomerQuery.countByProfileName(ADMIN_PROFILE) <= 1) {
            throw new LastAdminRemovalNotAllowedException();
        }
        customer.removeProfile(profile);
    }
}
