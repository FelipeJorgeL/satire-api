package br.com.api.satireapi.domain.inventory.internal.usecase.movement;

import br.com.api.satireapi.domain.inventory.internal.dto.request.AdminStockMovementFilter;
import br.com.api.satireapi.domain.inventory.internal.dto.response.AdminStockMovementResponse;
import br.com.api.satireapi.domain.inventory.internal.mapper.InventoryMapper;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementRepository;
import br.com.api.satireapi.domain.inventory.internal.persistence.StockMovementSpecifications;
import br.com.api.satireapi.domain.inventory.internal.usecase.InvalidStockMovementPeriodException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListAdminStockMovementsUseCase {

    private final StockMovementRepository movementRepository;

    public ListAdminStockMovementsUseCase(StockMovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Transactional(readOnly = true)
    public Page<AdminStockMovementResponse> execute(
        AdminStockMovementFilter filter,
        Pageable pageable
    ) {
        validatePeriod(filter);
        return movementRepository.findAll(StockMovementSpecifications.from(filter), pageable)
            .map(InventoryMapper::toResponse);
    }

    private void validatePeriod(AdminStockMovementFilter filter) {
        if (filter.from() != null && filter.to() != null && filter.from().isAfter(filter.to())) {
            throw new InvalidStockMovementPeriodException();
        }
    }
}
