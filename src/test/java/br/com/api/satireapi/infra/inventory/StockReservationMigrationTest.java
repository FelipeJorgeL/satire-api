package br.com.api.satireapi.infra.inventory;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class StockReservationMigrationTest {

    @Test
    void createsUniqueOrderReservationsAndReservationItems() throws Exception {
        var migration = Files.readString(
            Path.of("src/main/resources/db/migration/V6__create_stock_reservations.sql"),
            StandardCharsets.UTF_8
        );

        assertTrue(migration.contains("pedido_id UUID NOT NULL UNIQUE"));
        assertTrue(migration.contains("status IN ('ACTIVE', 'RELEASED', 'CONFIRMED', 'EXPIRED')"));
        assertTrue(migration.contains("CREATE TABLE itens_reservas_estoque"));
        assertTrue(migration.contains("UNIQUE (reserva_id, variacao_produto_id)"));
    }
}
