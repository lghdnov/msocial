package lghdnov.msocial.feature.post.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Пост")
public record PostDTO(
    @Schema(description = "Идентификатор поста", example = "1")
    Long id,

    @Schema(description = "Идентификатор автора", example = "1")
    Long authorId,

    @Schema(description = "Имя автора (внешний Matrix ID)", example = "@user:example.org")
    String authorName,

    @Schema(description = "Содержимое поста", example = "Привет, мир!")
    String content,

    @Schema(description = "Медиафайлы поста")
    List<PostMediaDTO> media,

    @Schema(description = "Опубликован")
    Boolean published,

    @Schema(description = "Дата создания")
    Instant createdAt
) {}
