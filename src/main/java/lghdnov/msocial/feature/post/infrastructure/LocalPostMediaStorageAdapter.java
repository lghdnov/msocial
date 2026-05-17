package lghdnov.msocial.feature.post.infrastructure;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.post.api.PostMediaStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Локальный адаптер хранения медиафайлов постов.
 *
 * <p>Реализует {@link PostMediaStoragePort}, сохраняя файлы в файловую систему.
 * Путь настраивается через {@code app.storage.local.post-media-path}.
 */
@Component
class LocalPostMediaStorageAdapter implements PostMediaStoragePort {

    private final String postMediaPath;

    LocalPostMediaStorageAdapter(
        @Value("${app.storage.local.post-media-path:./uploads/post-media}") String postMediaPath
    ) {
        this.postMediaPath = postMediaPath;
    }

    @Override
    public String uploadPostMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("POST_MEDIA_EMPTY", "Файл медиа не может быть пустым");
        }
        String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
        return saveFile(file, postMediaPath, filename, "/post-media/");
    }

    @Override
    public void delete(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        try {
            if (!url.startsWith("/post-media/")) {
                throw new ValidationException("INVALID_PATH", "Некорректный префикс пути файла");
            }
            String filename = Paths.get(url).getFileName().toString();
            Path filePath = Paths.get(postMediaPath, filename).toAbsolutePath().normalize();
            Path basePath = Paths.get(postMediaPath).toAbsolutePath().normalize();
            if (!filePath.startsWith(basePath)) {
                throw new ValidationException("INVALID_PATH", "Некорректный путь файла");
            }
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ValidationException("DELETE_FAILED", "Не удалось удалить файл: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file, String directory, String filename, String urlPrefix) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            Path filePath = dirPath.resolve(filename).toAbsolutePath().normalize();
            file.transferTo(filePath);
            return urlPrefix + filename;
        } catch (IOException e) {
            throw new ValidationException("UPLOAD_FAILED", "Не удалось сохранить файл: " + e.getMessage());
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
