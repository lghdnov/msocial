package lghdnov.msocial.feature.post.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Медиафайл поста")
public record PostMediaDTO(
    @Schema(description = "Идентификатор медиа", example = "1")
    Long id,

    @Schema(description = "URL медиафайла", example = "/post-media/uuid.jpg")
    String url,

    @Schema(description = "Порядок сортировки", example = "0")
    Integer sortOrder
) {}
