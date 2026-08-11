package br.com.api.satireapi.domain.payment.internal.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentWebhookVerifier {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final byte[] secret;
    private final Duration tolerance;
    private final Clock clock;

    @Autowired
    public PaymentWebhookVerifier(
        @Value("${app.payment.webhook.secret:}") String secret,
        @Value("${app.payment.webhook.tolerance:PT5M}") Duration tolerance
    ) {
        this(secret, tolerance, Clock.systemUTC());
    }

    PaymentWebhookVerifier(String secret, Duration tolerance, Clock clock) {
        this.secret = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        this.tolerance = tolerance;
        this.clock = clock;
    }

    public void verify(
        String eventId,
        String timestampValue,
        String signatureValue,
        String rawPayload
    ) {
        if (secret.length < 32 || tolerance == null || tolerance.isNegative()
            || tolerance.isZero()) {
            throw new IllegalStateException("Payment webhook security is not configured");
        }
        var timestamp = parseTimestamp(timestampValue);
        if (Duration.between(timestamp, clock.instant()).abs().compareTo(tolerance) > 0) {
            throw new InvalidPaymentWebhookSignatureException();
        }

        var canonicalPayload = timestampValue + "." + eventId + "." + rawPayload;
        var expected = sign(canonicalPayload);
        var provided = decodeSignature(signatureValue);
        if (!MessageDigest.isEqual(expected, provided)) {
            throw new InvalidPaymentWebhookSignatureException();
        }
    }

    private Instant parseTimestamp(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new InvalidPaymentWebhookSignatureException();
        }
    }

    private byte[] sign(String value) {
        try {
            var mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("HMAC algorithm is unavailable", exception);
        }
    }

    private byte[] decodeSignature(String value) {
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new InvalidPaymentWebhookSignatureException();
        }
    }
}
