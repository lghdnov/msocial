package lghdnov.msocial.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.ErrorResponse;
import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Tag(name = "Users", description = "Управление профилем пользователя")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;

    public UserController(UserQueryPort userQueryPort, UserCommandPort userCommandPort) {
        this.userQueryPort = userQueryPort;
        this.userCommandPort = userCommandPort;
    }

    @Operation(summary = "Получить профиль текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль найден",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userQueryPort.getProfile(userId));
    }

    @Operation(summary = "Обновить профиль текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль обновлён",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Пользователь не найден",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
        Principal principal,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userCommandPort.updateProfile(userId, request));
    }

    @Operation(summary = "Получить историю аватаров")
    @ApiResponse(responseCode = "200", description = "История аватаров",
        content = @Content(schema = @Schema(implementation = AvatarDTO.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/profile/avatars")
    public ResponseEntity<List<AvatarDTO>> getAvatarHistory(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userQueryPort.getAvatarHistory(userId));
    }

    @Operation(summary = "Загрузить аватар")
    @ApiResponse(responseCode = "200", description = "Аватар загружен",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "Файл некорректен",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadAvatar(
        Principal principal,
        @Parameter(description = "Файл аватара (изображение)", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userCommandPort.uploadAvatar(userId, file));
    }

    @Operation(summary = "Загрузить любимый трек")
    @ApiResponse(responseCode = "200", description = "Трек загружен",
        content = @Content(schema = @Schema(implementation = UserDTO.class)))
    @ApiResponse(responseCode = "400", description = "Файл некорректен",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(value = "/profile/track", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadTrack(
        Principal principal,
        @Parameter(description = "Аудиофайл трека", required = true)
        @RequestParam("file") MultipartFile file
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userCommandPort.uploadTrack(userId, file));
    }

    private Long resolveUserId(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new AccessDeniedException("UNAUTHORIZED", "Пользователь не аутентифицирован");
        }
        try {
            return Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new AccessDeniedException("INVALID_PRINCIPAL", "Некорректный идентификатор пользователя");
        }
    }
}
