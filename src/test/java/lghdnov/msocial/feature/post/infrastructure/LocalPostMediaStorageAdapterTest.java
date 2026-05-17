package lghdnov.msocial.feature.post.infrastructure;

import lghdnov.msocial.common.exceptions.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalPostMediaStorageAdapterTest {

    private Path tempPostMediaDir;
    private LocalPostMediaStorageAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        tempPostMediaDir = Files.createTempDirectory("post-media");
        adapter = new LocalPostMediaStorageAdapter(tempPostMediaDir.toString());
    }

    @Test
    void uploadPostMedia_shouldSaveFileAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});

        String url = adapter.uploadPostMedia(file);

        assertThat(url).startsWith("/post-media/");
        assertThat(url).endsWith(".jpg");
        Path saved = tempPostMediaDir.resolve(url.substring("/post-media/".length()));
        assertThat(saved).exists();
    }

    @Test
    void uploadPostMedia_shouldThrow_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[]{});

        assertThatThrownBy(() -> adapter.uploadPostMedia(emptyFile))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Файл медиа не может быть пустым");
    }

    @Test
    void delete_shouldRemovePostMediaFile() {
        MockMultipartFile file = new MockMultipartFile("file", "del.jpg", "image/jpeg", new byte[]{4, 5, 6});
        String url = adapter.uploadPostMedia(file);

        adapter.delete(url);

        Path saved = tempPostMediaDir.resolve(url.substring("/post-media/".length()));
        assertThat(saved).doesNotExist();
    }

    @Test
    void delete_shouldThrow_whenInvalidPathPrefix() {
        assertThatThrownBy(() -> adapter.delete("/avatars/image.jpg"))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Некорректный префикс пути файла");
    }

    @Test
    void delete_shouldDoNothing_whenUrlIsBlank() {
        assertThatNoException().isThrownBy(() -> adapter.delete(" "));
        assertThatNoException().isThrownBy(() -> adapter.delete(null));
    }
}
