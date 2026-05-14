package lghdnov.msocial.feature.user.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Персональная информация пользователя")
public record PersonalInfoDTO(
    @Schema(description = "День рождения", example = "1990-05-20")
    LocalDate birthDate,

    @Schema(description = "Адрес", example = "г. Москва, ул. Пушкина, д. 10")
    String address,

    @Schema(description = "Ссылка на любимый трек", example = "/uploads/tracks/track-uuid.mp3")
    String favoriteTrackUrl,

    @Schema(description = "Статус", example = "В поиске себя")
    String status
) {}
