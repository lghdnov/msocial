package lghdnov.msocial.feature.auth.api;

import lghdnov.msocial.feature.auth.entity.Session;

import java.util.Optional;

/**
 * Порт управления сессиями аутентификации.
 *
 * @implNote Реализация ({@code SessionService}) хранит сессии и refresh-токены в PostgreSQL.
 *           При ротации refresh-токена старый токен аннулируется (revoke).
 * @see lghdnov.msocial.feature.auth.entity.Session
 */
public interface SessionManagementPort {

    /**
     * Создаёт новую сессию для пользователя.
     *
     * @param userId идентификатор локального пользователя
     * @return созданная сущность сессии
     */
    Session createSession(Long userId);

    /**
     * Отзывает (удаляет) сессию по её идентификатору.
     *
     * @param sessionId идентификатор сессии
     */
    void revokeSession(Long sessionId);

    /**
     * Находит активную сессию по refresh-токену.
     *
     * @param refreshToken строковое значение refresh-токена
     * @return активная сессия, если найдена
     */
    Optional<Session> findActiveSession(String refreshToken);
}
