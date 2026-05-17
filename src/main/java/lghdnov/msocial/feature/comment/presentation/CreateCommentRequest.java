package lghdnov.msocial.feature.comment.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание комментария")
public record CreateCommentRequest(
    @NotBlank(message = "Содержимое комментария не может быть пустым")
    @Size(max = 2000, message = "Содержимое комментария не должно превышать 2000 символов")
    @Schema(description = "Содержимое комментария", example = "Отличный пост!")
    String content,

    @Schema(description = "Идентификатор родительского комментария (для ответов)", example = "1")
    Long parentId
) {}
