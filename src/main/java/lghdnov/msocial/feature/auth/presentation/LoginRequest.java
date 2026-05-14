package lghdnov.msocial.feature.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос на аутентификацию через Matrix OpenID")
public record LoginRequest(
    @NotBlank(message = "OpenID токен обязателен")
    @Schema(description = "Токен, полученный от Matrix-сервера", example = "openid_token_xyz")
    String openidToken,

    @Schema(description = "Идентификатор пользователя (используется только в dev-режиме при отключённой верификации)", example = "@user:example.org")
    String userId
) {}
