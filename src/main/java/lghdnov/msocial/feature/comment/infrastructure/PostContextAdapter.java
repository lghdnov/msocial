package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.comment.api.PostContextPort;
import lghdnov.msocial.feature.post.api.PostQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Адаптер проверки контекста поста.
 *
 * <p>Реализует {@link PostContextPort}, делегируя вызовы в {@code PostQueryPort}
 * из модуля {@code feature::post}.
 */
@Component
@RequiredArgsConstructor
class PostContextAdapter implements PostContextPort {

    private final PostQueryPort postQueryPort;

    @Override
    public boolean isPostVisible(Long postId) {
        try {
            postQueryPort.getPost(null, postId);
            return true;
        } catch (NotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean allowsComments(Long postId) {
        // В текущей версии комментирование разрешено для всех видимых постов.
        // При необходимости можно расширить PostQueryPort для передачи флага commentsEnabled.
        return isPostVisible(postId);
    }
}
