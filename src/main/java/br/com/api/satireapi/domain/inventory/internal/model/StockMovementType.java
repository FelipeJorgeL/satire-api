package br.com.api.satireapi.domain.inventory.internal.model;

public enum StockMovementType {
    ENTRADA(1),
    SAIDA(-1),
    VENDA(-1);

    private final int sign;

    StockMovementType(int sign) {
        this.sign = sign;
    }

    public int delta(int quantity) {
        return Math.multiplyExact(sign, quantity);
    }
}
