package lghdnov.msocial.feature.user.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Запрос на обновление профиля")
public record ProfileUpdateRequest(
    @Schema(description = "День рождения", example = "1990-05-20")
    LocalDate birthDate,

    @Size(max = 500, message = "Адрес не может превышать 500 символов")
    @Schema(description = "Адрес", example = "г. Москва, ул. Пушкина, д. 10")
    String address,



    @Size(max = 255, message = "Статус не может превышать 255 символов")
    @Schema(description = "Статус", example = "В поиске себя")
    String status
) {}
