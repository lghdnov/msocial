package lghdnov.msocial.feature.comment.api;

import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Порт чтения данных комментария.
 *
 * @implNote Реализация ({@code CommentService}) выполняет выборку через JPA-репозитории
 *           с учётом фильтрации удалённых комментариев.
 * @see lghdnov.msocial.feature.comment.service.CommentService
 */
public interface CommentQueryPort {

    /**
     * Возвращает страницу корневых комментариев к посту.
     *
     * @param postId   идентификатор поста
     * @param pageable параметры пагинации
     * @return страница комментариев
     */
    Page<CommentDTO> getByPostId(Long postId, Pageable pageable);

    /**
     * Возвращает комментарий по идентификатору.
     *
     * @param commentId идентификатор комментария
     * @return DTO комментария
     * @throws lghdnov.msocial.common.exceptions.NotFoundException если комментарий не найден или удалён
     */
    CommentDTO getById(Long commentId);

    /**
     * Возвращает страницу ответов (replies) на комментарий.
     *
     * @param parentId идентификатор родительского комментария
     * @param pageable параметры пагинации
     * @return страница ответов
     * @throws lghdnov.msocial.common.exceptions.NotFoundException если родительский комментарий не найден
     */
    Page<CommentDTO> getReplies(Long parentId, Pageable pageable);
}
