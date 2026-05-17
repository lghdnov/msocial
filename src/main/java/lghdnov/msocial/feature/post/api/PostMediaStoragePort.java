package lghdnov.msocial.feature.post.api;

import org.springframework.web.multipart.MultipartFile;

/**
 * Порт хранения медиафайлов постов.
 */
public interface PostMediaStoragePort {

    String uploadPostMedia(MultipartFile file);

    void delete(String url);
}
