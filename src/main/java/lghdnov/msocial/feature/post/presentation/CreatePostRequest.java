package lghdnov.msocial.feature.post.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на создание поста")
public record CreatePostRequest(
    @NotBlank(message = "Содержимое поста обязательно")
    @Size(max = 5000, message = "Содержимое поста не может превышать 5000 символов")
    @Schema(description = "Содержимое поста", example = "Привет, мир!")
    String content
) {}
