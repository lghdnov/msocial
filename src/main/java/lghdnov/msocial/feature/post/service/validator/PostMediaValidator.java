package lghdnov.msocial.feature.post.service.validator;

import lghdnov.msocial.common.exceptions.ValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Валидатор медиафайлов поста.
 */
@Component
public class PostMediaValidator {

    private static final int MAX_FILES = 10;
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public void validate(List<MultipartFile> files, int existingCount) {
        if (files == null || files.isEmpty()) {
            throw new ValidationException("MEDIA_EMPTY", "Список файлов не может быть пустым");
        }
        if (existingCount + files.size() > MAX_FILES) {
            throw new ValidationException("MEDIA_TOO_MANY", "Максимум " + MAX_FILES + " файлов на пост");
        }
        for (MultipartFile file : files) {
            validateFile(file);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("MEDIA_FILE_EMPTY", "Файл не может быть пустым");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ValidationException("MEDIA_TOO_LARGE", "Максимальный размер файла 10 МБ");
        }
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.startsWith("image/") || contentType.startsWith("video/"))) {
            throw new ValidationException("MEDIA_TYPE_INVALID", "Допустимы только изображения и видео");
        }
    }
}
