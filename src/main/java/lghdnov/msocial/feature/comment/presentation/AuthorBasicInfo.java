package lghdnov.msocial.feature.comment.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Базовая информация об авторе")
public record AuthorBasicInfo(
    @Schema(description = "Отображаемое имя автора", example = "@user:example.org")
    String name
) {}
