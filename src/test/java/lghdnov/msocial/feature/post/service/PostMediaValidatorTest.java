package lghdnov.msocial.feature.post.service;

import lghdnov.msocial.common.exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostMediaValidatorTest {

    private final PostMediaValidator validator = new PostMediaValidator();

    @Test
    void validate_shouldPass_forValidFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[1024]);
        assertThatNoException().isThrownBy(() -> validator.validate(List.of(file), 0));
    }

    @Test
    void validate_shouldThrow_whenNullList() {
        assertThatThrownBy(() -> validator.validate(null, 0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Список файлов не может быть пустым");
    }

    @Test
    void validate_shouldThrow_whenEmptyList() {
        assertThatThrownBy(() -> validator.validate(List.of(), 0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Список файлов не может быть пустым");
    }

    @Test
    void validate_shouldThrow_whenTooManyFiles() {
        List<org.springframework.web.multipart.MultipartFile> files = List.of(
            new MockMultipartFile("f", "1.jpg", "image/jpeg", new byte[100]),
            new MockMultipartFile("f", "2.jpg", "image/jpeg", new byte[100])
        );

        assertThatThrownBy(() -> validator.validate(files, 9))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Максимум 10 файлов на пост");
    }

    @Test
    void validate_shouldThrow_whenFileTooLarge() {
        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", new byte[11 * 1024 * 1024]);

        assertThatThrownBy(() -> validator.validate(List.of(file), 0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Максимальный размер файла 10 МБ");
    }

    @Test
    void validate_shouldThrow_whenInvalidType() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[1024]);

        assertThatThrownBy(() -> validator.validate(List.of(file), 0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Допустимы только изображения и видео");
    }

    @Test
    void validate_shouldThrow_whenEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[]{});

        assertThatThrownBy(() -> validator.validate(List.of(file), 0))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Файл не может быть пустым");
    }
}
