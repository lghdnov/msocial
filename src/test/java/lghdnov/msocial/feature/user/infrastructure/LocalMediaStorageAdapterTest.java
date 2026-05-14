package lghdnov.msocial.feature.user.infrastructure;

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

class LocalMediaStorageAdapterTest {

    private Path tempAvatarDir;
    private Path tempTrackDir;
    private LocalMediaStorageAdapter adapter;

    @BeforeEach
    void setUp() throws IOException {
        tempAvatarDir = Files.createTempDirectory("avatars");
        tempTrackDir = Files.createTempDirectory("tracks");
        adapter = new LocalMediaStorageAdapter(tempAvatarDir.toString(), tempTrackDir.toString());
    }

    @Test
    void uploadAvatar_shouldSaveFileAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2, 3});

        String url = adapter.uploadAvatar(file);

        assertThat(url).startsWith("/avatars/");
        assertThat(url).endsWith(".png");
        Path saved = tempAvatarDir.resolve(url.substring("/avatars/".length()));
        assertThat(saved).exists();
    }

    @Test
    void uploadTrack_shouldSaveFileAndReturnUrl() {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{4, 5, 6});

        String url = adapter.uploadTrack(file);

        assertThat(url).startsWith("/tracks/");
        assertThat(url).endsWith(".mp3");
        Path saved = tempTrackDir.resolve(url.substring("/tracks/".length()));
        assertThat(saved).exists();
    }

    @Test
    void uploadAvatar_shouldThrow_whenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.png", "image/png", new byte[]{});

        assertThatThrownBy(() -> adapter.uploadAvatar(emptyFile))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Файл аватара не может быть пустым");
    }

    @Test
    void delete_shouldRemoveAvatarFile() {
        MockMultipartFile file = new MockMultipartFile("file", "del.png", "image/png", new byte[]{7, 8, 9});
        String url = adapter.uploadAvatar(file);

        adapter.delete(url);

        Path saved = tempAvatarDir.resolve(url.substring("/avatars/".length()));
        assertThat(saved).doesNotExist();
    }

    @Test
    void delete_shouldDoNothing_whenUrlIsBlank() {
        assertThatNoException().isThrownBy(() -> adapter.delete(" "));
        assertThatNoException().isThrownBy(() -> adapter.delete(null));
    }
}
