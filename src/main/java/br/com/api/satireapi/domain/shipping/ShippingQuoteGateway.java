package br.com.api.satireapi.domain.shipping;

public interface ShippingQuoteGateway {

    ShippingQuote quote(ShippingQuoteRequest request);
}
