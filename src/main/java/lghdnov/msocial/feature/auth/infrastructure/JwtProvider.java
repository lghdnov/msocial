package lghdnov.msocial.feature.auth.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

/**
 * Инфраструктурный компонент для криптографических операций с JWT.
 *
 * <p>Не содержит бизнес-логики. Подписывает и верифицирует токены
 * с использованием HMAC-SHA256.
 */
@Component
public class JwtProvider {

    @Value("${auth.jwt.secret:defaultSecretKeyForDevelopmentOnlyDoNotUseInProduction}")
    private String secret;

    @Value("${auth.jwt.access-token-expiration-seconds:900}")
    private long accessTokenExpirationSeconds;

    private SecretKey key;

    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String signClaims(Long userId, JwtClaims claims) {
        Instant now = Instant.now();
        Instant expiration = now.plusSeconds(accessTokenExpirationSeconds);

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("roles", claims.roles() != null ? claims.roles() : Set.of())
            .claim("sessionId", claims.sessionId())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(key)
            .compact();
    }

    public Claims verifySignature(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }
}
