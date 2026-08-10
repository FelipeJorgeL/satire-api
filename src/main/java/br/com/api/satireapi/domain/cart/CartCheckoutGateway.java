package br.com.api.satireapi.domain.cart;

import java.util.UUID;

public interface CartCheckoutGateway {

    CartCheckoutSnapshot loadForCheckout(UUID customerId);

    void clear(UUID customerId);
}
