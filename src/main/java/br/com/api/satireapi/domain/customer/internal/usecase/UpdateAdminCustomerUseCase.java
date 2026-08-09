package br.com.api.satireapi.domain.customer.internal.usecase;

import br.com.api.satireapi.domain.customer.internal.dto.request.UpdateAdminCustomerRequest;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateAdminCustomerUseCase {

    private final AdminCustomerQuery adminCustomerQuery;

    public UpdateAdminCustomerUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional
    public void execute(UUID customerId, UpdateAdminCustomerRequest request) {
        var customer = adminCustomerQuery.findByIdForUpdate(customerId)
            .orElseThrow(AdminCustomerNotFoundException::new);
        customer.updateAdministrativeData(request.name(), request.email(), request.cpf(), request.phone());
    }
}
