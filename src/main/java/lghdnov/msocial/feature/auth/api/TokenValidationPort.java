package lghdnov.msocial.feature.auth.api;

import lghdnov.msocial.feature.auth.entity.JwtClaims;

/**
 * Порт валидации внутренних JWT.
 *
 * @implNote Реализация ({@code AuthService}) использует {@code JwtProvider} для
 *           проверки подписи и срока действия токена.
 * @see lghdnov.msocial.feature.auth.infrastructure.JwtProvider
 */
public interface TokenValidationPort {

    /**
     * Проверяет подпись и срок действия access-токена.
     *
     * @param token строковое представление JWT
     * @return {@code true}, если токен валиден
     */
    boolean validateToken(String token);

    /**
     * Извлекает кастомные claims из токена без полной валидации.
     *
     * @param token строковое представление JWT
     * @return объект {@link JwtClaims} с sub, roles, sessionId и т.д.
     * @throws lghdnov.msocial.common.exceptions.ValidationException если токен повреждён
     */
    JwtClaims extractClaims(String token);
}
