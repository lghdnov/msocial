package lghdnov.msocial.feature.comment.api;

import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;

/**
 * Порт управления комментариями (создание, изменение, удаление).
 *
 * @implNote Реализация ({@code CommentService}) выполняет валидацию,
 *           проверку прав доступа и мягкое удаление.
 * @see lghdnov.msocial.feature.comment.service.CommentService
 */
public interface CommentCommandPort {

    /**
     * Создаёт комментарий к посту.
     *
     * @param userId  идентификатор автора
     * @param postId  идентификатор поста
     * @param request данные комментария
     * @return созданный комментарий
     * @throws lghdnov.msocial.common.exceptions.ValidationException        если данные некорректны
     * @throws lghdnov.msocial.common.exceptions.NotFoundException          если пост не найден или скрыт
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException      если комментирование запрещено
     */
    CommentDTO create(Long userId, Long postId, CreateCommentRequest request);

    /**
     * Обновляет содержимое комментария.
     *
     * @param userId    идентификатор пользователя
     * @param commentId идентификатор комментария
     * @param request   новые данные
     * @return обновлённый комментарий
     * @throws lghdnov.msocial.common.exceptions.ValidationException   если данные некорректны
     * @throws lghdnov.msocial.common.exceptions.NotFoundException     если комментарий не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не является автором
     */
    CommentDTO update(Long userId, Long commentId, UpdateCommentRequest request);

    /**
     * Выполняет мягкое удаление комментария.
     *
     * @param userId    идентификатор пользователя
     * @param commentId идентификатор комментария
     * @throws lghdnov.msocial.common.exceptions.NotFoundException     если комментарий не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не является автором
     */
    void delete(Long userId, Long commentId);
}
