package lghdnov.msocial.feature.auth.api;

import lghdnov.msocial.feature.auth.entity.MatrixUserInfo;

/**
 * Порт верификации внешних OIDC-токенов через Matrix Federation API.
 *
 * @implNote Реализация ({@code MatrixFederationAdapter}) выполняет HTTP-вызов
 *           {@code POST /_matrix/federation/v1/openid/userinfo}.
 * @see lghdnov.msocial.feature.auth.infrastructure.MatrixFederationAdapter
 */
public interface OidcVerificationPort {

    /**
     * Подтверждает {@code openid_token} через сервер федерации Matrix.
     *
     * @param openidToken токен, полученный клиентом от Matrix-сервера
     * @return информация о пользователе Matrix
     * @throws lghdnov.msocial.common.exceptions.ValidationException если токен недействителен
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если сервер федерации отклонил запрос
     */
    MatrixUserInfo verifyOpenIdToken(String openidToken);
}
