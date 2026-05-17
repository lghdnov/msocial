package lghdnov.msocial.feature.comment.service.validator;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentValidatorTest {

    private final CommentValidator validator = new CommentValidator();

    @Test
    void validateCreate_shouldPass_whenContentValid() {
        validator.validateCreate(new CreateCommentRequest("Отличный пост!", null));
    }

    @Test
    void validateCreate_shouldThrow_whenContentNull() {
        assertThatThrownBy(() -> validator.validateCreate(new CreateCommentRequest(null, null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Содержимое комментария не может быть пустым");
    }

    @Test
    void validateCreate_shouldThrow_whenContentBlank() {
        assertThatThrownBy(() -> validator.validateCreate(new CreateCommentRequest("   ", null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Содержимое комментария не может быть пустым");
    }

    @Test
    void validateCreate_shouldThrow_whenContentTooLong() {
        String longContent = "a".repeat(2001);
        assertThatThrownBy(() -> validator.validateCreate(new CreateCommentRequest(longContent, null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("не должно превышать 2000 символов");
    }

    @Test
    void validateCreate_shouldThrow_whenContentIsSpam() {
        String spam = "a".repeat(51);
        assertThatThrownBy(() -> validator.validateCreate(new CreateCommentRequest(spam, null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("спам-повторы");
    }

    @Test
    void validateUpdate_shouldPass_whenContentValid() {
        validator.validateUpdate(new UpdateCommentRequest("Обновлённый текст"));
    }

    @Test
    void validateUpdate_shouldThrow_whenContentBlank() {
        assertThatThrownBy(() -> validator.validateUpdate(new UpdateCommentRequest("")))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Содержимое комментария не может быть пустым");
    }

    @Test
    void validateCreate_shouldPass_whenRequestNull() {
        assertThatThrownBy(() -> validator.validateCreate(null))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Запрос на создание комментария не может быть пустым");
    }
}
