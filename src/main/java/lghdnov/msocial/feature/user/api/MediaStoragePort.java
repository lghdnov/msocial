package lghdnov.msocial.feature.user.api;

import org.springframework.web.multipart.MultipartFile;

/**
 * Порт хранения медиафайлов (аватары, треки и др.).
 *
 * @implNote Реализация ({@code LocalMediaStorageAdapter}) сохраняет файлы
 *           в локальную файловую систему. При смене типа хранилища
 *           достаточно заменить адаптер, не меняя бизнес-логику.
 * @see lghdnov.msocial.feature.user.infrastructure.LocalMediaStorageAdapter
 */
public interface MediaStoragePort {

    /**
     * Загружает файл аватара и возвращает публичный URL/путь.
     *
     * @param file файл аватара
     * @return относительный URL сохранённого файла
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файл пустой или некорректный
     */
    String uploadAvatar(MultipartFile file);

    /**
     * Загружает музыкальный трек и возвращает публичный URL/путь.
     *
     * @param file файл трека
     * @return относительный URL сохранённого файла
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файл пустой или некорректный
     */
    String uploadTrack(MultipartFile file);

    /**
     * Удаляет файл по его URL/пути.
     *
     * @param url относительный URL файла
     */
    void delete(String url);
}
