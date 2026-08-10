package br.com.api.satireapi.domain.inventory;

import java.util.List;
import java.util.UUID;

public interface StockSaleGateway {

    void registerSale(UUID orderId, UUID customerId, List<StockSaleLine> lines);
}
