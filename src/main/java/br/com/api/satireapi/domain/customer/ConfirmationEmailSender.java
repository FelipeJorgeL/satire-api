package br.com.api.satireapi.domain.customer;

public interface ConfirmationEmailSender {

    void sendEmailConfirmation(String to, String confirmationLink);
}
