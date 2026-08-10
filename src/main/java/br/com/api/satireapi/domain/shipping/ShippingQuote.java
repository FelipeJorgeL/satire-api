package br.com.api.satireapi.domain.shipping;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShippingQuote(
    String service,
    BigDecimal fee,
    LocalDate estimatedDelivery
) {
}
