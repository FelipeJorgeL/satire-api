package br.com.api.satireapi.domain.customer.internal.usecase;

public interface ConfirmationEmailSender {

    void sendEmailConfirmation(String to, String confirmationLink);
}
