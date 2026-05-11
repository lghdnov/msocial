package lghdnov.msocial.feature.auth.service;

import lghdnov.msocial.feature.auth.api.TokenGenerationPort;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.infrastructure.JwtProvider;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Сервис генерации токенов.
 *
 * <p>Реализует {@link TokenGenerationPort}, делегируя подпись JWT
 * инфраструктурному {@link JwtProvider}.
 */
@Service
class TokenService implements TokenGenerationPort {

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
}
