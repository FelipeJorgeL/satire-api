package br.com.api.satireapi.domain.order;

import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderGateway {

    Optional<PaymentOrder> findOwnedForUpdate(UUID customerId, UUID orderId);

    boolean isOwnedBy(UUID customerId, UUID orderId);

    void markPaid(UUID orderId);
}
