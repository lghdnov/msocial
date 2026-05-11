package lghdnov.msocial.feature.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на обновление пары токенов")
public record RefreshRequest(
    @NotBlank(message = "Refresh-токен обязателен")
    @Schema(description = "Действующий refresh-токен", example = "refresh_abc123")
    String refreshToken
) {}
