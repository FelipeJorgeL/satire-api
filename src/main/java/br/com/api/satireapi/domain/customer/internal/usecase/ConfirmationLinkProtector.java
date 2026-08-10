package br.com.api.satireapi.domain.customer.internal.usecase;

public interface ConfirmationLinkProtector {

    String protect(String confirmationLink);

    String unprotect(String protectedLink);
}
