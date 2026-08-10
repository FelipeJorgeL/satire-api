package br.com.api.satireapi.domain.shipping.internal.quote;

import br.com.api.satireapi.domain.shipping.ShippingQuote;
import br.com.api.satireapi.domain.shipping.ShippingQuoteGateway;
import br.com.api.satireapi.domain.shipping.ShippingQuoteRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
class DeterministicShippingQuoteGateway implements ShippingQuoteGateway {

    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("250.00");

    @Override
    public ShippingQuote quote(ShippingQuoteRequest request) {
        Objects.requireNonNull(request, "Shipping quote request is required");
        validate(request);
        var fee = calculateFee(request);
        var days = deliveryDays(request.postalCode());
        return new ShippingQuote(
            "STANDARD-SIMULATED", fee, LocalDate.now().plusDays(days)
        );
    }

    private static BigDecimal calculateFee(ShippingQuoteRequest request) {
        if (request.subtotal().compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        var fee = switch (region(request.postalCode())) {
            case 0, 1 -> new BigDecimal("14.90");
            case 2, 3, 4 -> new BigDecimal("19.90");
            default -> new BigDecimal("24.90");
        };
        var extraItems = Math.max(0, request.itemCount() - 3);
        return fee.add(BigDecimal.valueOf(extraItems).multiply(new BigDecimal("1.50")));
    }

    private static int deliveryDays(String postalCode) {
        return switch (region(postalCode)) {
            case 0, 1 -> 5;
            case 2, 3, 4 -> 7;
            default -> 10;
        };
    }

    private static int region(String postalCode) {
        return Integer.parseInt(postalCode.substring(0, 1));
    }

    private static void validate(ShippingQuoteRequest request) {
        if (request.postalCode() == null || !request.postalCode().matches("[0-9]{8}")) {
            throw new IllegalArgumentException("Postal code must contain 8 digits");
        }
        if (request.subtotal() == null || request.subtotal().signum() < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        if (request.itemCount() <= 0) {
            throw new IllegalArgumentException("Item count must be positive");
        }
    }
}
