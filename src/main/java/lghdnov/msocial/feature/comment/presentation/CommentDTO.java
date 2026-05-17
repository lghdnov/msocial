package lghdnov.msocial.feature.comment.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import lghdnov.msocial.feature.comment.entity.CommentStatus;

import java.time.Instant;

@Schema(description = "Комментарий")
public record CommentDTO(
    @Schema(description = "Идентификатор комментария", example = "1")
    Long id,

    @Schema(description = "Идентификатор поста", example = "1")
    Long postId,

    @Schema(description = "Идентификатор автора", example = "1")
    Long authorId,

    @Schema(description = "Имя автора (внешний Matrix ID)", example = "@user:example.org")
    String authorName,

    @Schema(description = "Идентификатор родительского комментария (для ответов)", example = "1")
    Long parentId,

    @Schema(description = "Содержимое комментария", example = "Отличный пост!")
    String content,

    @Schema(description = "Статус комментария")
    CommentStatus status,

    @Schema(description = "Дата создания")
    Instant createdAt,

    @Schema(description = "Дата последнего обновления")
    Instant updatedAt
) {}
