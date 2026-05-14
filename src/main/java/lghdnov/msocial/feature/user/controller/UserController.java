package lghdnov.msocial.feature.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lghdnov.msocial.feature.user.api.UserCommandPort;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
        Long userId = Long.valueOf(principal.getName());
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
        Long userId = Long.valueOf(principal.getName());
        return ResponseEntity.ok(userCommandPort.updateProfile(userId, request));
    }
}
