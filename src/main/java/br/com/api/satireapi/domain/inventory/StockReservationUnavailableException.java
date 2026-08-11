package br.com.api.satireapi.domain.inventory;

public class StockReservationUnavailableException extends RuntimeException {

    public StockReservationUnavailableException() {
        super("The requested stock is unavailable for reservation");
    }
}
