package lghdnov.msocial.common.exceptions;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при ошибке")
public record ErrorResponse(
    @Schema(description = "Код ошибки", example = "VALIDATION_ERROR")
    String code,

    @Schema(description = "Сообщение об ошибке", example = "Некорректные данные")
    String message
) {}
