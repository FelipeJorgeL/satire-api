package br.com.api.satireapi.infra.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generatesAndParsesValidToken() {
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        var service = new JwtTokenService(SECRET, 3600, clock);

        var token = service.generate("customer-id", "felipe@example.com", List.of("CLIENTE", "ADMIN"));
        var claims = service.parse(token).orElseThrow();

        assertEquals("customer-id", claims.getSubject());
        assertEquals("felipe@example.com", claims.get("email", String.class));
        assertEquals(List.of("CLIENTE", "ADMIN"), claims.get("profiles", List.class));
    }

    @Test
    void rejectsExpiredToken() {
        var issuedAt = Instant.parse("2026-01-01T00:00:00Z");
        var issuer = new JwtTokenService(SECRET, 1, Clock.fixed(issuedAt, ZoneOffset.UTC));
        var token = issuer.generate("customer-id", "felipe@example.com", List.of("CLIENTE"));

        var verifier = new JwtTokenService(SECRET, 1, Clock.fixed(issuedAt.plusSeconds(10), ZoneOffset.UTC));

        assertTrue(verifier.parse(token).isEmpty());
    }

    @Test
    void rejectsTokenSignedWithDifferentSecret() {
        var clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        var issuer = new JwtTokenService(SECRET, 3600, clock);
        var verifier = new JwtTokenService("98765432109876543210987654321098", 3600, clock);

        var token = issuer.generate("customer-id", "felipe@example.com", List.of("CLIENTE"));

        assertTrue(verifier.parse(token).isEmpty());
    }

    @Test
    void rejectsMalformedToken() {
        var service = new JwtTokenService(SECRET, 3600, Clock.systemUTC());

        assertTrue(service.parse("not-a-valid-token").isEmpty());
    }
}
