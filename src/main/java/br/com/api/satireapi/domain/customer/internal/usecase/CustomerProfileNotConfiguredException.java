package br.com.api.satireapi.domain.customer.internal.usecase;

public class CustomerProfileNotConfiguredException extends IllegalStateException {

    public CustomerProfileNotConfiguredException() {
        super("Required CLIENTE profile is not configured");
    }
}
