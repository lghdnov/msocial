package lghdnov.msocial.feature.auth.entity;

/**
 * Информация о пользователе, полученная от Matrix Federation API.
 *
 * @param sub Matrix User ID (например, {@code @user:example.org})
 */
public record MatrixUserInfo(
    String sub
) {}
