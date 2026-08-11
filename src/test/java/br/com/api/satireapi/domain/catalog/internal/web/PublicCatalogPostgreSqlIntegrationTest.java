package br.com.api.satireapi.domain.catalog.internal.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.AddFavoriteUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.ListFavoritesUseCase;
import br.com.api.satireapi.domain.catalog.internal.usecase.favorite.RemoveFavoriteUseCase;
import br.com.api.satireapi.domain.order.OrderPaymentNotAllowedException;
import br.com.api.satireapi.domain.order.internal.usecase.OrderShipmentNotFoundException;
import br.com.api.satireapi.domain.order.internal.usecase.ReviewPurchaseRequiredException;
import br.com.api.satireapi.domain.order.internal.usecase.query.GetCustomerOrderShipmentUseCase;
import br.com.api.satireapi.domain.order.internal.dto.request.UpsertReviewRequest;
import br.com.api.satireapi.domain.order.internal.usecase.review.DeleteCustomerReviewUseCase;
import br.com.api.satireapi.domain.order.internal.usecase.review.UpsertCustomerReviewUseCase;
import br.com.api.satireapi.domain.payment.internal.dto.request.CreatePaymentRequest;
import br.com.api.satireapi.domain.payment.internal.model.PaymentMethod;
import br.com.api.satireapi.domain.payment.internal.usecase.creation.CreatePaymentUseCase;
import br.com.api.satireapi.domain.payment.internal.usecase.webhook.ProcessPaymentWebhookUseCase;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = {
    "app.jwt.secret=test-secret-key-with-at-least-32-bytes",
    "app.payment.webhook.secret=test-payment-webhook-secret-with-32-bytes",
    "app.mail.outbox.poll-interval=PT1H",
    "app.inventory.reservation-expiration-interval=PT1H"
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class PublicCatalogPostgreSqlIntegrationTest {

    @Container
    private static final PostgreSQLContainer DATABASE =
        new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DATABASE::getJdbcUrl);
        registry.add("spring.datasource.username", DATABASE::getUsername);
        registry.add("spring.datasource.password", DATABASE::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AddFavoriteUseCase addFavoriteUseCase;

    @Autowired
    private ListFavoritesUseCase listFavoritesUseCase;

    @Autowired
    private RemoveFavoriteUseCase removeFavoriteUseCase;

    @Autowired
    private CreatePaymentUseCase createPaymentUseCase;

    @Autowired
    private ProcessPaymentWebhookUseCase paymentWebhookUseCase;

    @Autowired
    private GetCustomerOrderShipmentUseCase getCustomerOrderShipmentUseCase;

    @Autowired
    private UpsertCustomerReviewUseCase upsertCustomerReviewUseCase;

    @Autowired
    private DeleteCustomerReviewUseCase deleteCustomerReviewUseCase;

    @Test
    void servesOnlyActiveProductsFromActiveCategories() throws Exception {
        var activeCategoryId = UUID.randomUUID();
        var inactiveCategoryId = UUID.randomUUID();
        var visibleProductId = UUID.randomUUID();
        var hiddenByCategoryProductId = UUID.randomUUID();
        var inactiveProductId = UUID.randomUUID();
        var visibleVariationId = UUID.randomUUID();
        var inactiveVariationId = UUID.randomUUID();
        var imageId = UUID.randomUUID();

        insertCategory(activeCategoryId, "Active category", "active-category", true);
        insertCategory(inactiveCategoryId, "Inactive category", "inactive-category", false);
        insertProduct(visibleProductId, activeCategoryId, "Visible product", "visible-product", true);
        insertProduct(
            hiddenByCategoryProductId, inactiveCategoryId,
            "Hidden category product", "hidden-category-product", true
        );
        insertProduct(inactiveProductId, activeCategoryId, "Inactive product", "inactive-product", false);
        insertVariation(visibleVariationId, visibleProductId, "SKU-VISIBLE", true);
        insertVariation(inactiveVariationId, visibleProductId, "SKU-INACTIVE", false);
        jdbcTemplate.update(
            "INSERT INTO imagens_produtos "
                + "(id, produto_id, url, texto_alternativo, principal, ordem_exibicao, decorativa) "
                + "VALUES (?, ?, ?, ?, TRUE, 0, FALSE)",
            imageId, visibleProductId, "https://cdn.example.com/product.jpg", "Visible product"
        );

        mockMvc.perform(get("/api/v1/products").param("categoryId", activeCategoryId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].id").value(visibleProductId.toString()))
            .andExpect(jsonPath("$.content[0].minimumPrice").value(99.90));

        mockMvc.perform(get("/api/v1/products/" + visibleProductId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.variations.length()").value(1))
            .andExpect(jsonPath("$.images.length()").value(1));

        mockMvc.perform(get(
                "/api/v1/products/" + visibleProductId
                    + "/variations/" + visibleVariationId + "/availability"
            ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.available").value(true))
            .andExpect(jsonPath("$.availableQuantity").value(5));

        mockMvc.perform(get(
                "/api/v1/products/" + visibleProductId
                    + "/variations/" + inactiveVariationId + "/availability"
            ))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/products/" + hiddenByCategoryProductId))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/products/" + inactiveProductId))
            .andExpect(status().isNotFound());
    }

    @Test
    void addsListsAndRemovesFavoriteIdempotently() {
        var customerId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var productId = UUID.randomUUID();

        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Customer', ?, 'hash')",
            customerId, customerId + "@example.com"
        );
        insertCategory(categoryId, "Favorite category", "favorite-category", true);
        insertProduct(productId, categoryId, "Favorite product", "favorite-product", true);

        addFavoriteUseCase.execute(customerId, productId);
        addFavoriteUseCase.execute(customerId, productId);

        var favorites = listFavoritesUseCase.execute(customerId, PageRequest.of(0, 20));
        org.junit.jupiter.api.Assertions.assertEquals(1, favorites.getTotalElements());
        org.junit.jupiter.api.Assertions.assertEquals(
            productId, favorites.getContent().get(0).product().id()
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            1L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM favoritos WHERE usuario_id = ? AND produto_id = ?",
                Long.class,
                customerId,
                productId
            )
        );

        removeFavoriteUseCase.execute(customerId, productId);
        removeFavoriteUseCase.execute(customerId, productId);

        org.junit.jupiter.api.Assertions.assertEquals(
            0L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM favoritos WHERE usuario_id = ? AND produto_id = ?",
                Long.class,
                customerId,
                productId
            )
        );
    }

    @Test
    void createsAndApprovesSimulatedPaymentIdempotently() throws Exception {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Payer', ?, 'hash')",
            customerId, customerId + "@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO pedidos "
                + "(id, usuario_id, numero, subtotal, desconto, frete, valor_total) "
                + "VALUES (?, ?, ?, 100.00, 0, 10.00, 110.00)",
            orderId, customerId, "PAY-" + orderId.toString().substring(0, 20)
        );

        var request = new CreatePaymentRequest(PaymentMethod.PIX);
        var first = createPaymentUseCase.execute(
            customerId, orderId, "payment-key-123456", request
        );
        var replay = createPaymentUseCase.execute(
            customerId, orderId, "payment-key-123456", request
        );

        org.junit.jupiter.api.Assertions.assertTrue(first.created());
        org.junit.jupiter.api.Assertions.assertFalse(replay.created());
        org.junit.jupiter.api.Assertions.assertEquals(first.payment().id(), replay.payment().id());
        org.junit.jupiter.api.Assertions.assertEquals(
            1L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM pagamentos WHERE pedido_id = ?", Long.class, orderId
            )
        );

        var eventId = "evt-" + UUID.randomUUID();
        var timestamp = Instant.now().toString();
        var payload = "{\"status\":\"APROVADO\","
            + "\"gatewayTransactionId\":\"txn-" + first.payment().id() + "\"}";
        var signature = paymentWebhookSignature(eventId, timestamp, payload);
        paymentWebhookUseCase.execute(
            first.payment().id(), eventId, timestamp, signature, payload
        );
        paymentWebhookUseCase.execute(
            first.payment().id(), eventId, timestamp, signature, payload
        );

        org.junit.jupiter.api.Assertions.assertEquals(
            "APROVADO",
            jdbcTemplate.queryForObject(
                "SELECT status FROM pagamentos WHERE id = ?", String.class, first.payment().id()
            )
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            "PAGO",
            jdbcTemplate.queryForObject(
                "SELECT status FROM pedidos WHERE id = ?", String.class, orderId
            )
        );
        org.junit.jupiter.api.Assertions.assertEquals(
            1L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM eventos_webhook_pagamentos WHERE evento_id = ?",
                Long.class,
                eventId
            )
        );

        org.junit.jupiter.api.Assertions.assertThrows(
            OrderPaymentNotAllowedException.class,
            () -> createPaymentUseCase.execute(
                customerId, orderId, "payment-key-654321", request
            )
        );
    }

    @Test
    void createsOnlyOnePaymentWhenIdempotentRequestsRace() {
        var customerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Concurrent payer', ?, 'hash')",
            customerId, customerId + "@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO pedidos "
                + "(id, usuario_id, numero, subtotal, desconto, frete, valor_total) "
                + "VALUES (?, ?, ?, 100.00, 0, 10.00, 110.00)",
            orderId, customerId, "RACE-" + orderId.toString().substring(0, 19)
        );

        var request = new CreatePaymentRequest(PaymentMethod.PIX);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = CompletableFuture.supplyAsync(
                () -> createPaymentUseCase.execute(
                    customerId, orderId, "concurrent-payment-key", request
                ),
                executor
            );
            var second = CompletableFuture.supplyAsync(
                () -> createPaymentUseCase.execute(
                    customerId, orderId, "concurrent-payment-key", request
                ),
                executor
            );

            var results = CompletableFuture.allOf(first, second)
                .thenApply(ignored -> java.util.List.of(first.join(), second.join()))
                .join();

            org.junit.jupiter.api.Assertions.assertEquals(
                results.get(0).payment().id(), results.get(1).payment().id()
            );
            org.junit.jupiter.api.Assertions.assertEquals(
                1, results.stream().filter(result -> result.created()).count()
            );
            org.junit.jupiter.api.Assertions.assertEquals(
                1L,
                jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pagamentos WHERE pedido_id = ?",
                    Long.class,
                    orderId
                )
            );
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void returnsShipmentOnlyToOrderOwner() {
        var customerId = UUID.randomUUID();
        var otherCustomerId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var shipmentId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Shipment owner', ?, 'hash')",
            customerId, customerId + "@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Other customer', ?, 'hash')",
            otherCustomerId, otherCustomerId + "@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO pedidos "
                + "(id, usuario_id, numero, subtotal, desconto, frete, valor_total) "
                + "VALUES (?, ?, ?, 50.00, 0, 5.00, 55.00)",
            orderId, customerId, "SHIP-" + orderId.toString().substring(0, 19)
        );
        jdbcTemplate.update(
            "INSERT INTO entregas "
                + "(id, pedido_id, transportadora, codigo_rastreio, status, previsao_entrega) "
                + "VALUES (?, ?, 'Correios', ?, 'EM_TRANSITO', CURRENT_DATE + 3)",
            shipmentId, orderId, "TRACK-" + shipmentId
        );

        var response = getCustomerOrderShipmentUseCase.execute(customerId, orderId);

        org.junit.jupiter.api.Assertions.assertEquals(shipmentId, response.id());
        org.junit.jupiter.api.Assertions.assertEquals(orderId, response.orderId());
        org.junit.jupiter.api.Assertions.assertEquals("EM_TRANSITO", response.status());
        org.junit.jupiter.api.Assertions.assertThrows(
            OrderShipmentNotFoundException.class,
            () -> getCustomerOrderShipmentUseCase.execute(otherCustomerId, orderId)
        );
    }

    @Test
    void upsertsListsAndDeletesVerifiedReviewSafelyUnderConcurrency() throws Exception {
        var customerId = UUID.randomUUID();
        var otherCustomerId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var productId = UUID.randomUUID();
        var variationId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'Reviewer', ?, 'hash')",
            customerId, customerId + "@example.com"
        );
        jdbcTemplate.update(
            "INSERT INTO usuarios (id, nome, email, senha_hash) VALUES (?, 'No purchase', ?, 'hash')",
            otherCustomerId, otherCustomerId + "@example.com"
        );
        insertCategory(categoryId, "Review category", "review-category", true);
        insertProduct(productId, categoryId, "Reviewed product", "reviewed-product", true);
        insertVariation(variationId, productId, "SKU-REVIEW", true);
        jdbcTemplate.update(
            "INSERT INTO pedidos "
                + "(id, usuario_id, numero, status, subtotal, desconto, frete, valor_total) "
                + "VALUES (?, ?, ?, 'ENTREGUE', 99.90, 0, 0, 99.90)",
            orderId, customerId, "REVIEW-" + orderId.toString().substring(0, 17)
        );
        jdbcTemplate.update(
            "INSERT INTO itens_pedidos "
                + "(id, pedido_id, variacao_produto_id, produto_id, sku, nome_produto, "
                + "nome_variacao, preco_unitario, quantidade, subtotal) "
                + "VALUES (?, ?, ?, ?, 'SKU-REVIEW', 'Reviewed product', "
                + "'Default', 99.90, 1, 99.90)",
            UUID.randomUUID(), orderId, variationId, productId
        );

        var request = new UpsertReviewRequest((short) 5, "  Excelente produto  ");
        var executor = Executors.newFixedThreadPool(2);
        try {
            var first = CompletableFuture.supplyAsync(
                () -> upsertCustomerReviewUseCase.execute(customerId, productId, request),
                executor
            );
            var second = CompletableFuture.supplyAsync(
                () -> upsertCustomerReviewUseCase.execute(customerId, productId, request),
                executor
            );
            var reviews = CompletableFuture.allOf(first, second)
                .thenApply(ignored -> java.util.List.of(first.join(), second.join()))
                .join();

            org.junit.jupiter.api.Assertions.assertEquals(
                reviews.get(0).id(), reviews.get(1).id()
            );
        } finally {
            executor.shutdownNow();
        }

        var updated = upsertCustomerReviewUseCase.execute(
            customerId, productId, new UpsertReviewRequest((short) 4, "Muito bom")
        );
        org.junit.jupiter.api.Assertions.assertEquals((short) 4, updated.rating());
        org.junit.jupiter.api.Assertions.assertEquals(
            1L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM avaliacoes WHERE usuario_id = ? AND produto_id = ?",
                Long.class,
                customerId,
                productId
            )
        );

        mockMvc.perform(get("/api/v1/products/" + productId + "/reviews"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].rating").value(4))
            .andExpect(jsonPath("$.content[0].comment").value("Muito bom"));

        org.junit.jupiter.api.Assertions.assertThrows(
            ReviewPurchaseRequiredException.class,
            () -> upsertCustomerReviewUseCase.execute(otherCustomerId, productId, request)
        );

        deleteCustomerReviewUseCase.execute(customerId, productId);
        deleteCustomerReviewUseCase.execute(customerId, productId);
        org.junit.jupiter.api.Assertions.assertEquals(
            0L,
            jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM avaliacoes WHERE usuario_id = ? AND produto_id = ?",
                Long.class,
                customerId,
                productId
            )
        );
    }

    private void insertCategory(UUID id, String name, String slug, boolean active) {
        jdbcTemplate.update(
            "INSERT INTO categorias (id, nome, slug, ativo, excluido_em) "
                + "VALUES (?, ?, ?, ?, CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END)",
            id, name, slug, active, active
        );
    }

    private void insertProduct(
        UUID id,
        UUID categoryId,
        String name,
        String slug,
        boolean active
    ) {
        jdbcTemplate.update(
            "INSERT INTO produtos (id, categoria_id, nome, slug, ativo, excluido_em) "
                + "VALUES (?, ?, ?, ?, ?, CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END)",
            id, categoryId, name, slug, active, active
        );
    }

    private void insertVariation(UUID id, UUID productId, String sku, boolean active) {
        jdbcTemplate.update(
            "INSERT INTO variacoes_produtos "
                + "(id, produto_id, sku, nome, preco, estoque, ativo, excluido_em) "
                + "VALUES (?, ?, ?, 'Default', 99.90, 5, ?, "
                + "CASE WHEN ? THEN NULL ELSE CURRENT_TIMESTAMP END)",
            id, productId, sku, active, active
        );
    }

    private String paymentWebhookSignature(
        String eventId,
        String timestamp,
        String payload
    ) throws Exception {
        var secret = "test-payment-webhook-secret-with-32-bytes";
        var canonicalPayload = timestamp + "." + eventId + "." + payload;
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            mac.doFinal(canonicalPayload.getBytes(StandardCharsets.UTF_8))
        );
    }
}
