package br.com.api.satireapi.domain.customer;

public interface ConfirmationLinkProtector {

    String protect(String confirmationLink);

    String unprotect(String protectedLink);
}
