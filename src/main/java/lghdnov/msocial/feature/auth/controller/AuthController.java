package lghdnov.msocial.feature.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lghdnov.msocial.feature.auth.api.AuthCommandPort;
import lghdnov.msocial.feature.auth.api.TokenValidationPort;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import lghdnov.msocial.feature.auth.presentation.LoginRequest;
import lghdnov.msocial.feature.auth.presentation.RefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Аутентификация и авторизация")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthCommandPort authCommandPort;
    private final TokenValidationPort tokenValidationPort;

    public AuthController(AuthCommandPort authCommandPort, TokenValidationPort tokenValidationPort) {
        this.authCommandPort = authCommandPort;
        this.tokenValidationPort = tokenValidationPort;
    }

    @Operation(summary = "Вход по Matrix OpenID токену")
    @ApiResponse(responseCode = "200", description = "Успешная аутентификация")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    @ApiResponse(responseCode = "403", description = "Доступ запрещён")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authCommandPort.login(request));
    }

    @Operation(summary = "Обновление токенов")
    @ApiResponse(responseCode = "200", description = "Токены обновлены")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    @ApiResponse(responseCode = "404", description = "Сессия не найдена")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authCommandPort.refresh(request));
    }

    @Operation(summary = "Выход из системы")
    @ApiResponse(responseCode = "204", description = "Сессия завершена")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("X-Refresh-Token") String refreshToken) {
        authCommandPort.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Валидация access-токена")
    @ApiResponse(responseCode = "200", description = "Токен валиден")
    @ApiResponse(responseCode = "400", description = "Токен недействителен")
    @GetMapping("/validate")
    public ResponseEntity<JwtClaims> validateToken(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replaceFirst("Bearer ", "");
        if (!tokenValidationPort.validateToken(token)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(tokenValidationPort.extractClaims(token));
    }
}
