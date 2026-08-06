package br.com.api.satireapi.infra.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenService {

    private final SecretKey signingKey;
    private final Duration expiration;
    private final Clock clock;

    @Autowired
    public JwtTokenService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration}") long expirationSeconds
    ) {
        this(secret, expirationSeconds, Clock.systemUTC());
    }

    JwtTokenService(String secret, long expirationSeconds, Clock clock) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofSeconds(expirationSeconds);
        this.clock = clock;
    }

    public String generate(String subject, String email, List<String> profiles) {
        var now = Instant.now(clock);
        return Jwts.builder()
            .subject(subject)
            .claim("email", email)
            .claim("profiles", profiles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(expiration)))
            .signWith(signingKey)
            .compact();
    }

    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(
                Jwts.parser()
                    .clock(() -> Date.from(Instant.now(clock)))
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
            );
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long expirationSeconds() {
        return expiration.getSeconds();
    }
}
