package br.com.api.satireapi.infra.mail;

import br.com.api.satireapi.domain.customer.internal.usecase.ConfirmationLinkProtector;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class ConfirmationLinkProtectorAdapter implements ConfirmationLinkProtector {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    ConfirmationLinkProtectorAdapter(@Value("${app.jwt.secret}") String secret) {
        try {
            this.key = new SecretKeySpec(
                MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)),
                "AES"
            );
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to initialize confirmation link protection", ex);
        }
    }

    @Override
    public String protect(String confirmationLink) {
        try {
            var iv = new byte[IV_BYTES];
            random.nextBytes(iv);
            var cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            var encrypted = cipher.doFinal(confirmationLink.getBytes(StandardCharsets.UTF_8));
            var payload = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, iv.length);
            System.arraycopy(encrypted, 0, payload, iv.length, encrypted.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(payload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Unable to protect confirmation link", ex);
        }
    }

    @Override
    public String unprotect(String protectedLink) {
        try {
            var payload = Base64.getUrlDecoder().decode(protectedLink);
            if (payload.length <= IV_BYTES) {
                throw new IllegalArgumentException("Invalid protected confirmation link");
            }
            var iv = new byte[IV_BYTES];
            var encrypted = new byte[payload.length - IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, iv.length);
            System.arraycopy(payload, iv.length, encrypted, 0, encrypted.length);
            var cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Unable to unprotect confirmation link", ex);
        }
    }
}
