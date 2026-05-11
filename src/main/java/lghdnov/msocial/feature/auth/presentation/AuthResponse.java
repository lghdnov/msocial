package lghdnov.msocial.feature.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с токенами доступа")
public record AuthResponse(
    @Schema(description = "JWT access-токен", example = "eyJhbGciOiJIUzI1NiJ9...")
    String accessToken,

    @Schema(description = "Refresh-токен для обновления сессии", example = "dB9xK2mP...")
    String refreshToken,

    @Schema(description = "Время жизни access-токена в секундах", example = "900")
    long expiresIn
) {}
