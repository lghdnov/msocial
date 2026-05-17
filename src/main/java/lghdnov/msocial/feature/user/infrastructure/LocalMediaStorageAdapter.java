package lghdnov.msocial.feature.user.infrastructure;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.user.api.MediaStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Локальный адаптер хранения медиафайлов.
 *
 * <p>
 * Реализует {@link MediaStoragePort}, сохраняя файлы в файловую систему.
 * Путь настраивается через {@code app.storage.local.avatar-path} и
 * {@code app.storage.local.track-path}.
 */
@Component
class LocalMediaStorageAdapter implements MediaStoragePort {

  private final String avatarPath;
  private final String trackPath;

  LocalMediaStorageAdapter(
      @Value("${app.storage.local.avatar-path:./uploads/avatars}") String avatarPath,
      @Value("${app.storage.local.track-path:./uploads/tracks}") String trackPath) {
    this.avatarPath = avatarPath;
    this.trackPath = trackPath;
  }

  @Override
  public String uploadAvatar(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ValidationException("AVATAR_EMPTY", "Файл аватара не может быть пустым");
    }
    String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
    return saveFile(file, avatarPath, filename, "/avatars/");
  }

  @Override
  public String uploadTrack(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ValidationException("TRACK_EMPTY", "Файл трека не может быть пустым");
    }
    String filename = UUID.randomUUID() + getExtension(file.getOriginalFilename());
    return saveFile(file, trackPath, filename, "/tracks/");
  }

  @Override
  public void delete(String url) {
    if (url == null || url.isBlank()) {
      return;
    }
    try {
      String baseDir;
      if (url.startsWith("/avatars/")) {
        baseDir = avatarPath;
      } else if (url.startsWith("/tracks/")) {
        baseDir = trackPath;
      } else {
        throw new ValidationException("INVALID_PATH", "Некорректный префикс пути файла");
      }
      String filename = Paths.get(url).getFileName().toString();
      Path filePath = Paths.get(baseDir, filename).toAbsolutePath().normalize();
      Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
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
