package lghdnov.msocial.feature.user.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Аватар пользователя")
public record AvatarDTO(
    @Schema(description = "Идентификатор аватара", example = "1")
    Long id,

    @Schema(description = "URL аватара", example = "/avatars/uuid.jpg")
    String url,

    @Schema(description = "Дата загрузки")
    Instant uploadedAt,

    @Schema(description = "Активен ли аватар", example = "true")
    Boolean active
) {}
