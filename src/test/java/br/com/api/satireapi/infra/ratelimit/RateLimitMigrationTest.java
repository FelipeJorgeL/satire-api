package br.com.api.satireapi.infra.ratelimit;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RateLimitMigrationTest {

    @Test
    void createsAConcurrentlySafeBucketTable() throws Exception {
        var migration = Files.readString(
            Path.of("src/main/resources/db/migration/V5__create_rate_limit_buckets.sql"),
            StandardCharsets.UTF_8
        );

        assertTrue(migration.contains("chave VARCHAR(80) PRIMARY KEY"));
        assertTrue(migration.contains("tentativas INTEGER NOT NULL"));
        assertTrue(migration.contains("atualizado_em TIMESTAMPTZ"));
        assertTrue(Files.readString(
            Path.of("src/main/java/br/com/api/satireapi/infra/ratelimit/JpaRateLimitBucketStore.java"),
            StandardCharsets.UTF_8
        ).contains("ON CONFLICT (chave) DO UPDATE"));
    }
}
