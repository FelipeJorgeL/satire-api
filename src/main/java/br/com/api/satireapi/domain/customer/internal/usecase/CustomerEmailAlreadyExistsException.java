package br.com.api.satireapi.domain.customer.internal.usecase;

// Sem @ResponseStatus de propósito: a existência de um e-mail nunca deve chegar ao cliente HTTP.
// O controller de registro captura esta exceção e devolve a mesma resposta do fluxo de sucesso.
public class CustomerEmailAlreadyExistsException extends RuntimeException {

    public CustomerEmailAlreadyExistsException() {
        super("Email is already registered");
    }
}
