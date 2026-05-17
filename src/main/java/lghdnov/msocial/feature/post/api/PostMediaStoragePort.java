package lghdnov.msocial.feature.post.api;

import org.springframework.web.multipart.MultipartFile;

/**
 * Порт хранения медиафайлов постов.
 *
 * @implNote Реализация ({@code LocalPostMediaStorageAdapter}) сохраняет файлы
 *           в локальную файловую систему или внешнее хранилище (S3, CDN).
 *           URL возвращается в формате, пригодном для публичного доступа.
 * @see lghdnov.msocial.feature.post.infrastructure.LocalPostMediaStorageAdapter
 */
public interface PostMediaStoragePort {

    /**
     * Загружает медиафайл поста и возвращает публичный URL.
     *
     * @param file загружаемый файл
     * @return URL сохранённого файла
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файл пустой или загрузка не удалась
     */
    String uploadPostMedia(MultipartFile file);

    /**
     * Удаляет медиафайл по URL.
     *
     * @param url URL файла, ранее возвращённый {@link #uploadPostMedia}
     */
    void delete(String url);
}
