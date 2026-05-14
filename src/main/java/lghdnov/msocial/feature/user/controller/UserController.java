package lghdnov.msocial.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@Tag(name = "Users", description = "Управление профилем пользователя")
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
    @ApiResponse(responseCode = "200", description = "Профиль найден")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userQueryPort.getProfile(userId));
    }

    @Operation(summary = "Обновить профиль текущего пользователя")
    @ApiResponse(responseCode = "200", description = "Профиль обновлён")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
        Principal principal,
        @Valid @RequestBody ProfileUpdateRequest request
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userCommandPort.updateProfile(userId, request));
    }

    @Operation(summary = "Получить историю аватаров")
    @ApiResponse(responseCode = "200", description = "История аватаров")
    @GetMapping("/profile/avatars")
    public ResponseEntity<List<AvatarDTO>> getAvatarHistory(Principal principal) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userQueryPort.getAvatarHistory(userId));
    }

    @Operation(summary = "Загрузить аватар")
    @ApiResponse(responseCode = "200", description = "Аватар загружен")
    @ApiResponse(responseCode = "400", description = "Файл некорректен")
    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadAvatar(
        Principal principal,
        @RequestParam("file") MultipartFile file
    ) {
        Long userId = resolveUserId(principal);
        return ResponseEntity.ok(userCommandPort.uploadAvatar(userId, file));
    }

    @Operation(summary = "Загрузить любимый трек")
    @ApiResponse(responseCode = "200", description = "Трек загружен")
    @ApiResponse(responseCode = "400", description = "Файл некорректен")
    @PostMapping(value = "/profile/track", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadTrack(
        Principal principal,
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
