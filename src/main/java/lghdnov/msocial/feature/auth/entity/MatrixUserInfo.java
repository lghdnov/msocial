package lghdnov.msocial.feature.auth.entity;

import java.time.Instant;

/**
 * Информация о пользователе, полученная от Matrix Federation API.
 *
 * @param sub      Matrix User ID (например, {@code @user:example.org})
 * @param avatarUrl URL аватара в Matrix (опционально)
 * @param displayName отображаемое имя (опционально)
 */
public record MatrixUserInfo(
    String sub,
    String avatarUrl,
    String displayName
) {}
