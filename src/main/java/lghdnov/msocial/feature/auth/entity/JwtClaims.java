package lghdnov.msocial.feature.auth.entity;

import java.util.Set;

/**
 * Кастомные claims внутреннего JWT.
 *
 * @param sub       идентификатор пользователя (как строка)
 * @param roles     набор ролей
 * @param sessionId идентификатор сессии
 */
public record JwtClaims(
    String sub,
    Set<String> roles,
    Long sessionId
) {}
