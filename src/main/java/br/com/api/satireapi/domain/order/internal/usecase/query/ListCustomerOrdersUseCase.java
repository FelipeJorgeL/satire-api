package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.dto.request.CustomerOrderFilter;
import br.com.api.satireapi.domain.order.internal.dto.response.CustomerOrderResponse;
import br.com.api.satireapi.domain.order.internal.mapper.OrderMapper;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderSpecifications;
import br.com.api.satireapi.domain.order.internal.usecase.InvalidOrderPeriodException;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListCustomerOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListCustomerOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerOrderResponse> execute(
        UUID customerId,
        CustomerOrderFilter filter,
        Pageable pageable
    ) {
        validateCustomer(customerId);
        validatePeriod(filter.from(), filter.to());
        return orderRepository.findAll(OrderSpecifications.forCustomer(customerId, filter), pageable)
            .map(OrderMapper::toCustomerListItem);
    }

    private static void validateCustomer(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer is required");
        }
    }

    private static void validatePeriod(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidOrderPeriodException();
        }
    }
}
