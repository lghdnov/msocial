package lghdnov.msocial.feature.post.api;

import lghdnov.msocial.feature.post.presentation.PostDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Порт чтения данных поста.
 *
 * @implNote Реализация ({@code PostService}) выполняет выборку через JPA-репозитории
 *           с учётом soft-delete фильтрации.
 * @see lghdnov.msocial.feature.post.service.PostService
 */
public interface PostQueryPort {

    /**
     * Возвращает пост по идентификатору.
     *
     * @param userId идентификатор текущего пользователя
     * @param postId идентификатор поста
     * @return DTO поста с медиа
     * @throws lghdnov.msocial.common.exceptions.NotFoundException если пост не найден или удалён
     */
    PostDTO getPost(Long userId, Long postId);

    /**
     * Возвращает ленту постов пользователя с пагинацией.
     *
     * @param authorId идентификатор автора
     * @param pageable параметры пагинации
     * @return страница постов
     */
    Page<PostDTO> getFeed(Long authorId, Pageable pageable);
}
