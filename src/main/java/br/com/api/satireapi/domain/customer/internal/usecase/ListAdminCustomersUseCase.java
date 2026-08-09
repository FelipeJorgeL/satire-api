package br.com.api.satireapi.domain.customer.internal.usecase;

import br.com.api.satireapi.domain.customer.internal.dto.request.AdminCustomerFilter;
import br.com.api.satireapi.domain.customer.internal.dto.response.AdminCustomerListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAdminCustomersUseCase {

    private final AdminCustomerQuery adminCustomerQuery;

    public ListAdminCustomersUseCase(AdminCustomerQuery adminCustomerQuery) {
        this.adminCustomerQuery = adminCustomerQuery;
    }

    @Transactional(readOnly = true)
    public Page<AdminCustomerListItemResponse> execute(AdminCustomerFilter filter, Pageable pageable) {
        return adminCustomerQuery.findAll(filter, pageable).map(customer -> new AdminCustomerListItemResponse(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.isActive(),
            customer.getCreatedAt()
        ));
    }
}
