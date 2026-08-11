package br.com.api.satireapi.domain.order.internal.web;

import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderShipmentUseCase;
import br.com.api.satireapi.domain.shipping.CustomerShipment;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
class CustomerOrderShippingController {

    private final GetCustomerOrderShipmentUseCase getCustomerOrderShipmentUseCase;

    CustomerOrderShippingController(GetCustomerOrderShipmentUseCase useCase) {
        this.getCustomerOrderShipmentUseCase = useCase;
    }

    @GetMapping("/{orderId}/shipping")
    CustomerShipment get(
        @AuthenticationPrincipal UUID customerId,
        @PathVariable UUID orderId
    ) {
        return getCustomerOrderShipmentUseCase.execute(customerId, orderId);
    }
}
