package lghdnov.msocial.feature.post.api;

import lghdnov.msocial.feature.post.presentation.CreatePostRequest;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.UpdatePostRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Порт изменения данных поста.
 *
 * @implNote Реализация ({@code PostService}) выполняет CRUD операции,
 *           валидацию медиа и управление медиафайлами через {@code PostMediaStoragePort}.
 * @see lghdnov.msocial.feature.post.service.PostService
 */
public interface PostCommandPort {

    /**
     * Создаёт новый пост.
     *
     * @param authorId идентификатор автора
     * @param request  данные поста
     * @return созданный пост
     * @throws lghdnov.msocial.common.exceptions.ValidationException если данные некорректны
     */
    PostDTO createPost(Long authorId, CreatePostRequest request);

    /**
     * Обновляет содержимое поста.
     *
     * @param userId  идентификатор текущего пользователя
     * @param postId  идентификатор поста
     * @param request новые данные
     * @return обновлённый пост
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пост не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не автор
     * @throws lghdnov.msocial.common.exceptions.ValidationException если данные некорректны
     */
    PostDTO updatePost(Long userId, Long postId, UpdatePostRequest request);

    /**
     * Удаляет пост (soft-delete).
     *
     * @param userId идентификатор текущего пользователя
     * @param postId идентификатор поста
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пост не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не автор
     */
    void deletePost(Long userId, Long postId);

    /**
     * Добавляет медиафайлы к посту.
     *
     * @param userId идентификатор текущего пользователя
     * @param postId идентификатор поста
     * @param files  список файлов
     * @return обновлённый пост
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пост не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не автор
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файлы некорректны
     */
    PostDTO addPostMedia(Long userId, Long postId, List<MultipartFile> files);

    /**
     * Удаляет медиафайл из поста.
     *
     * @param userId   идентификатор текущего пользователя
     * @param postId   идентификатор поста
     * @param mediaId  идентификатор медиа
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пост или медиа не найдены
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не автор
     */
    void deletePostMedia(Long userId, Long postId, Long mediaId);

    /**
     * Публикует пост.
     *
     * @param userId идентификатор текущего пользователя
     * @param postId идентификатор поста
     * @return опубликованный пост
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пост не найден
     * @throws lghdnov.msocial.common.exceptions.AccessDeniedException если пользователь не автор
     */
    PostDTO publishPost(Long userId, Long postId);
}
