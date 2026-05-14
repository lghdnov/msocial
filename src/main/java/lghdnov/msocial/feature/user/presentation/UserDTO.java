package lghdnov.msocial.feature.user.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Профиль пользователя")
public record UserDTO(
    @Schema(description = "Локальный идентификатор", example = "1")
    Long id,

    @Schema(description = "Внешний Matrix ID", example = "@user:example.org")
    String externalId,

    @Schema(description = "Персональная информация")
    PersonalInfoDTO personalInfo,

    @Schema(description = "Дата создания")
    Instant createdAt
) {}
