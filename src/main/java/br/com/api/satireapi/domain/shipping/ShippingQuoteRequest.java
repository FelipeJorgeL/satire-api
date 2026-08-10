package br.com.api.satireapi.domain.shipping;

import java.math.BigDecimal;

public record ShippingQuoteRequest(
    String postalCode,
    BigDecimal subtotal,
    int itemCount
) {
}
