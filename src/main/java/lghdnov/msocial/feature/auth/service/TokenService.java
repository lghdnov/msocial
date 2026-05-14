package lghdnov.msocial.feature.auth.service;

import io.jsonwebtoken.Claims;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.auth.api.TokenGenerationPort;
import lghdnov.msocial.feature.auth.api.TokenValidationPort;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.infrastructure.JwtProvider;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

/**
 * Сервис генерации и валидации токенов.
 *
 * <p>Реализует {@link TokenGenerationPort} и {@link TokenValidationPort},
 * делегируя криптографические операции инфраструктурному {@link JwtProvider}.
 */
@Service
class TokenService implements TokenGenerationPort, TokenValidationPort {

    private static final int REFRESH_TOKEN_BYTES = 64;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final JwtProvider jwtProvider;

    TokenService(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public String generateAccessToken(Long userId, JwtClaims claims) {
        return jwtProvider.signClaims(userId, claims);
    }

    @Override
    public String generateRefreshToken(Long sessionId) {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE64_ENCODER.encodeToString(bytes);
    }

    @Override
    public long getAccessTokenExpirationSeconds() {
        return jwtProvider.getAccessTokenExpirationSeconds();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return jwtProvider.verifySignature(token) != null;
    }

    @Override
    public JwtClaims extractClaims(String token) {
        Claims claims = jwtProvider.verifySignature(token);
        if (claims == null) {
            throw new ValidationException("JWT_INVALID", "Токен повреждён или истёк");
        }

        Long sessionId = claims.get("sessionId", Long.class);
        @SuppressWarnings("unchecked")
        Set<String> roles = claims.get("roles", Set.class);
        if (roles == null) {
            roles = Collections.emptySet();
        }

        return new JwtClaims(claims.getSubject(), roles, sessionId);
    }
}
