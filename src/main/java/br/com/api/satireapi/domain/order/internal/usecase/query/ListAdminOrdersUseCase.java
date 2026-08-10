package br.com.api.satireapi.domain.order.internal.usecase.query;

import br.com.api.satireapi.domain.order.internal.dto.request.AdminOrderFilter;
import br.com.api.satireapi.domain.order.internal.dto.response.AdminOrderListItemResponse;
import br.com.api.satireapi.domain.order.internal.mapper.OrderMapper;
import br.com.api.satireapi.domain.order.internal.persistence.OrderRepository;
import br.com.api.satireapi.domain.order.internal.persistence.OrderSpecifications;
import br.com.api.satireapi.domain.order.internal.usecase.InvalidOrderPeriodException;
import java.time.OffsetDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAdminOrdersUseCase {

    private final OrderRepository orderRepository;

    public ListAdminOrdersUseCase(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminOrderListItemResponse> execute(
        AdminOrderFilter filter,
        Pageable pageable
    ) {
        validatePeriod(filter.from(), filter.to());
        return orderRepository.findAll(OrderSpecifications.from(filter), pageable)
            .map(OrderMapper::toListItem);
    }

    private void validatePeriod(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidOrderPeriodException();
        }
    }
}
