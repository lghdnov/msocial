package lghdnov.msocial.feature.auth.api;

import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import lghdnov.msocial.feature.auth.presentation.LoginRequest;
import lghdnov.msocial.feature.auth.presentation.RefreshRequest;

/**
 * Порт команд аутентификации.
 *
 * @implNote Реализация ({@code AuthService}) выполняет полный цикл аутентификации:
 *           валидацию OIDC-токена, провижининг пользователя, генерацию JWT и создание сессии.
 * @see TokenValidationPort
 * @see lghdnov.msocial.feature.user.api.UserProvisioningPort
 */
public interface AuthCommandPort {

    /**
     * Аутентифицирует пользователя по Matrix OpenID токену.
     *
     * @param request запрос на вход, содержащий {@code openid_token}
     * @return ответ с {@code access_token}, {@code refresh_token} и {@code expires_in}
     * @throws lghdnov.msocial.common.exceptions.ValidationException если запрос некорректен
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если OIDC-токен отклонён
     */
    AuthResponse login(LoginRequest request);

    /**
     * Обновляет пару токенов по валидному refresh-токену.
     *
     * @param request запрос с {@code refresh_token}
     * @return новый {@link AuthResponse}
     * @throws lghdnov.msocial.common.exceptions.ValidationException если refresh-токен отсутствует или просрочен
     */
    AuthResponse refresh(RefreshRequest request);

    /**
     * Завершает сессию пользователя (logout).
     *
     * @param refreshToken refresh-токен активной сессии
     * @throws lghdnov.msocial.common.exceptions.ValidationException если сессия не найдена
     */
    void logout(String refreshToken);
}
