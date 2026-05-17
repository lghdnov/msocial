package lghdnov.msocial.feature.comment.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Список комментариев")
public record CommentListResponse(
    @Schema(description = "Комментарии")
    List<CommentDTO> comments,

    @Schema(description = "Общее количество элементов", example = "42")
    long totalElements
) {}
