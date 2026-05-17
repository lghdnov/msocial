package lghdnov.msocial.feature.comment.api;

/**
 * Порт для проверки контекста поста при работе с комментариями.
 *
 * <p>Определяется в {@code feature::comment} как consumer-driven contract
 * и реализуется в адаптере, который делегирует вызовы в {@code feature::post}.
 *
 * @implNote Реализация ({@code PostContextAdapter}) вызывает {@code PostQueryPort}
 *           для проверки видимости и статуса поста.
 * @see lghdnov.msocial.feature.comment.infrastructure.PostContextAdapter
 */
public interface PostContextPort {

    /**
     * Проверяет, виден ли пост для текущего пользователя.
     *
     * @param postId идентификатор поста
     * @return {@code true}, если пост существует и доступен
     */
    boolean isPostVisible(Long postId);

    /**
     * Проверяет, разрешено ли комментирование поста.
     *
     * @param postId идентификатор поста
     * @return {@code true}, если к посту можно оставлять комментарии
     */
    boolean allowsComments(Long postId);
}
