package lghdnov.msocial.feature.auth.api;

import lghdnov.msocial.feature.auth.entity.JwtClaims;

/**
 * Порт генерации внутренних JWT (access и refresh).
 *
 * @implNote Реализация ({@code TokenService}) делегирует криптографические операции
 *           {@code JwtProvider}. Access-токен stateless, refresh-токен — случайная строка,
 *           привязанная к сессии через {@link SessionManagementPort}.
 * @see lghdnov.msocial.feature.auth.infrastructure.JwtProvider
 */
public interface TokenGenerationPort {

    /**
     * Генерирует подписанный access-токен (JWT) на основе claims.
     *
     * @param userId идентификатор пользователя
     * @param claims дополнительные claims (роли, sessionId и т.д.)
     * @return строковое представление JWT
     */
    String generateAccessToken(Long userId, JwtClaims claims);

    /**
     * Генерирует криптографически стойкий refresh-токен.
     *
     * @param sessionId идентификатор сессии, к которой привязывается токен
     * @return refresh-токен
     */
    String generateRefreshToken(Long sessionId);

    /**
     * Возвращает время жизни access-токена в секундах.
     *
     * @return количество секунд
     */
    long getAccessTokenExpirationSeconds();
}
