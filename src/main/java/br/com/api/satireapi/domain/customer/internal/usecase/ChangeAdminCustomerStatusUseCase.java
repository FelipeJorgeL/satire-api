package br.com.api.satireapi.domain.customer.internal.usecase;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeAdminCustomerStatusUseCase {

    private final AdminCustomerQuery adminCustomerQuery;

    public ChangeAdminCustomerStatusUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional
    public void execute(UUID customerId, boolean active) {
        var customer = adminCustomerQuery.findByIdForUpdate(customerId)
            .orElseThrow(AdminCustomerNotFoundException::new);
        customer.changeStatus(active);
    }
}
