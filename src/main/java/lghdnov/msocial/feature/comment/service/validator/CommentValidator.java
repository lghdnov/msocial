package lghdnov.msocial.feature.comment.service.validator;

import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор комментариев.
 */
@Component
public class CommentValidator {

    private static final int MAX_CONTENT_LENGTH = 2000;
    private static final Pattern SPAM_PATTERN = Pattern.compile("(.)\\1{50,}");

    public void validateCreate(CreateCommentRequest request) {
        if (request == null) {
            throw new ValidationException("COMMENT_EMPTY", "Запрос на создание комментария не может быть пустым");
        }
        validateContent(request.content());
    }

    public void validateUpdate(UpdateCommentRequest request) {
        if (request == null) {
            throw new ValidationException("COMMENT_EMPTY", "Запрос на обновление комментария не может быть пустым");
        }
        validateContent(request.content());
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ValidationException("COMMENT_CONTENT_EMPTY", "Содержимое комментария не может быть пустым");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ValidationException("COMMENT_CONTENT_TOO_LONG",
                "Содержимое комментария не должно превышать " + MAX_CONTENT_LENGTH + " символов");
        }
        if (SPAM_PATTERN.matcher(content).find()) {
            throw new ValidationException("COMMENT_CONTENT_SPAM",
                "Содержимое комментария содержит спам-повторы");
        }
    }
}
