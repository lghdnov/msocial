package lghdnov.msocial.feature.comment.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на обновление комментария")
public record UpdateCommentRequest(
    @NotBlank(message = "Содержимое комментария не может быть пустым")
    @Size(max = 2000, message = "Содержимое комментария не должно превышать 2000 символов")
    @Schema(description = "Новое содержимое комментария", example = "Обновлённый текст")
    String content
) {}
