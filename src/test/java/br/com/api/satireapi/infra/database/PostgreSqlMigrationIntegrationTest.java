package br.com.api.satireapi.infra.database;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlMigrationIntegrationTest {

    @Container
    private static final PostgreSQLContainer DATABASE =
        new PostgreSQLContainer("postgres:17-alpine");

    @BeforeAll
    static void migrateSchema() {
        Flyway.configure()
            .dataSource(DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword())
            .load()
            .migrate();
    }

    @Test
    void supportsTheCompleteReservationMovementLifecycle() throws SQLException {
        var customerId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var orderId = UUID.randomUUID();

        try (var connection = connection()) {
            insertFixture(connection, customerId, categoryId, productId, variationId, orderId);
            insertMovement(connection, variationId, orderId, customerId, "RESERVA", 2, 10, 8);
            insertMovement(connection, variationId, orderId, customerId, "LIBERACAO", 2, 8, 10);
            insertMovement(connection, variationId, orderId, customerId, "VENDA", 2, 8, 8);

            try (var statement = connection.prepareStatement(
                "SELECT COUNT(*) FROM movimentacoes_estoque WHERE pedido_id = ?"
            )) {
                statement.setObject(1, orderId);
                try (var result = statement.executeQuery()) {
                    result.next();
                    assertEquals(3, result.getInt(1));
                }
            }
        }
    }

    private static Connection connection() throws SQLException {
        return DriverManager.getConnection(
            DATABASE.getJdbcUrl(), DATABASE.getUsername(), DATABASE.getPassword()
        );
    }

    private static void insertFixture(
        Connection connection,
        UUID customerId,
        UUID categoryId,
        UUID productId,
        UUID variationId,
        UUID orderId
    ) throws SQLException {
        execute(connection,
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Customer', ?, 'hash')",
            customerId, customerId + "@example.com");
        execute(connection,
            "INSERT INTO categorias (id, nome, slug) VALUES (?, ?, ?)",
            categoryId, "Category " + categoryId, "category-" + categoryId);
        execute(connection,
            "INSERT INTO produtos (id, categoria_id, nome, slug) VALUES (?, ?, ?, ?)",
            productId, categoryId, "Product " + productId, "product-" + productId);
        execute(connection,
            "INSERT INTO variacoes_produtos (id, produto_id, sku, nome, preco, estoque) "
                + "VALUES (?, ?, ?, 'Default', 10.00, 10)",
            variationId, productId, "SKU-" + variationId);
        execute(connection,
            "INSERT INTO pedidos (id, usuario_id, numero, subtotal, desconto, frete, valor_total) "
                + "VALUES (?, ?, ?, 20.00, 0, 0, 20.00)",
            orderId, customerId, "ORDER-" + orderId.toString().substring(0, 16));
    }

    private static void insertMovement(
        Connection connection,
        UUID variationId,
        UUID orderId,
        UUID customerId,
        String type,
        int quantity,
        int previousStock,
        int newStock
    ) throws SQLException {
        execute(connection,
            "INSERT INTO movimentacoes_estoque "
                + "(variacao_produto_id, pedido_id, usuario_id, tipo, quantidade, "
                + "estoque_anterior, estoque_novo) VALUES (?, ?, ?, ?, ?, ?, ?)",
            variationId, orderId, customerId, type, quantity, previousStock, newStock);
    }

    private static void execute(Connection connection, String sql, Object... parameters)
        throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (var index = 0; index < parameters.length; index++) {
                statement.setObject(index + 1, parameters[index]);
            }
            statement.executeUpdate();
        }
    }
}
