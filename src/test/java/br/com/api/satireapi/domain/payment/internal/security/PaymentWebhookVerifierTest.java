package br.com.api.satireapi.domain.payment.internal.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class PaymentWebhookVerifierTest {

    private static final String SECRET = "test-payment-webhook-secret-with-32-bytes";
    private static final Instant NOW = Instant.parse("2026-08-11T12:00:00Z");

    private final PaymentWebhookVerifier verifier = new PaymentWebhookVerifier(
        SECRET,
        Duration.ofMinutes(5),
        Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void acceptsValidSignatureWithinTolerance() throws Exception {
        var eventId = "evt-12345678";
        var timestamp = NOW.minusSeconds(60).toString();
        var payload = "{\"status\":\"APROVADO\",\"gatewayTransactionId\":\"txn-1\"}";

        assertDoesNotThrow(() -> verifier.verify(
            eventId, timestamp, sign(timestamp + "." + eventId + "." + payload), payload
        ));
    }

    @Test
    void rejectsInvalidSignatureAndStaleTimestamp() throws Exception {
        var eventId = "evt-12345678";
        var payload = "{}";
        var validTimestamp = NOW.toString();
        var staleTimestamp = NOW.minus(Duration.ofMinutes(6)).toString();

        assertThrows(
            InvalidPaymentWebhookSignatureException.class,
            () -> verifier.verify(eventId, validTimestamp, "invalid", payload)
        );
        assertThrows(
            InvalidPaymentWebhookSignatureException.class,
            () -> verifier.verify(
                eventId,
                staleTimestamp,
                sign(staleTimestamp + "." + eventId + "." + payload),
                payload
            )
        );
    }

    @Test
    void rejectsMalformedTimestamp() {
        assertThrows(
            InvalidPaymentWebhookSignatureException.class,
            () -> verifier.verify("evt-12345678", "not-a-timestamp", "invalid", "{}")
        );
    }

    private String sign(String value) throws Exception {
        var mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
            mac.doFinal(value.getBytes(StandardCharsets.UTF_8))
        );
    }
}
