package lghdnov.msocial.feature.auth.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.auth.api.*;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.entity.MatrixUserInfo;
import lghdnov.msocial.feature.auth.entity.Session;
import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import lghdnov.msocial.feature.auth.presentation.LoginRequest;
import lghdnov.msocial.feature.auth.presentation.RefreshRequest;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OidcVerificationPort oidcVerificationPort;

    @Mock
    private UserProvisioningPort userProvisioningPort;

    @Mock
    private TokenGenerationPort tokenGenerationPort;

    @Mock
    private SessionManagementPort sessionManagementPort;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokens_whenValidOpenIdToken() {
        String openidToken = "valid_token";
        String matrixSub = "@user:example.org";
        Long userId = 1L;
        String accessToken = "access_jwt";
        String refreshToken = "refresh_xyz";

        when(oidcVerificationPort.verifyOpenIdToken(openidToken))
            .thenReturn(new MatrixUserInfo(matrixSub));
        when(userProvisioningPort.findByIdOrCreate(matrixSub))
            .thenReturn(userId);
        when(userProvisioningPort.isAccountActive(userId)).thenReturn(true);

        Session session = Session.builder().id(10L).userId(userId).build();
        when(sessionManagementPort.createSession(userId)).thenReturn(session);
        when(tokenGenerationPort.generateRefreshToken(10L)).thenReturn(refreshToken);
        when(tokenGenerationPort.generateAccessToken(eq(userId), any(JwtClaims.class)))
            .thenReturn(accessToken);
        when(tokenGenerationPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.login(new LoginRequest(openidToken, null));

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.expiresIn()).isEqualTo(900L);
        verify(sessionManagementPort).updateRefreshToken(10L, refreshToken);
    }

    @Test
    void login_shouldThrow_whenOpenIdTokenIsBlank() {
        assertThatThrownBy(() -> authService.login(new LoginRequest(" ", null)))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("OpenID токен обязателен");
    }

    @Test
    void refresh_shouldReturnNewTokens_whenValidRefreshToken() {
        String oldRefresh = "old_refresh";
        String newAccess = "new_access";
        String newRefresh = "new_refresh";
        Long userId = 1L;

        Session oldSession = Session.builder().id(10L).userId(userId).build();
        when(sessionManagementPort.findActiveSession(oldRefresh)).thenReturn(Optional.of(oldSession));

        Session newSession = Session.builder().id(11L).userId(userId).build();
        when(sessionManagementPort.createSession(userId)).thenReturn(newSession);
        when(tokenGenerationPort.generateRefreshToken(11L)).thenReturn(newRefresh);
        when(tokenGenerationPort.generateAccessToken(eq(userId), any(JwtClaims.class)))
            .thenReturn(newAccess);
        when(tokenGenerationPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.refresh(new RefreshRequest(oldRefresh));

        assertThat(response.accessToken()).isEqualTo(newAccess);
        assertThat(response.refreshToken()).isEqualTo(newRefresh);
        verify(sessionManagementPort).revokeSession(10L);
        verify(sessionManagementPort).updateRefreshToken(11L, newRefresh);
    }

    @Test
    void refresh_shouldThrow_whenSessionNotFound() {
        when(sessionManagementPort.findActiveSession("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("invalid")))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Сессия не найдена или истекла");
    }

    @Test
    void login_shouldSkipVerificationAndUseUserId_whenDevModeEnabled() {
        String openidToken = "any_token";
        String devUserId = "@devuser:example.org";
        Long localUserId = 42L;
        String accessToken = "dev_access_jwt";
        String refreshToken = "dev_refresh_xyz";

        authService = new AuthService(
            oidcVerificationPort,
            userProvisioningPort,
            tokenGenerationPort,
            sessionManagementPort
        );
        // Set skipVerify = true via reflection
        try {
            java.lang.reflect.Field field = AuthService.class.getDeclaredField("skipVerify");
            field.setAccessible(true);
            field.setBoolean(authService, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        when(userProvisioningPort.findByIdOrCreate(devUserId))
            .thenReturn(localUserId);
        when(userProvisioningPort.isAccountActive(localUserId)).thenReturn(true);

        Session session = Session.builder().id(99L).userId(localUserId).build();
        when(sessionManagementPort.createSession(localUserId)).thenReturn(session);
        when(tokenGenerationPort.generateRefreshToken(99L)).thenReturn(refreshToken);
        when(tokenGenerationPort.generateAccessToken(eq(localUserId), any(JwtClaims.class)))
            .thenReturn(accessToken);
        when(tokenGenerationPort.getAccessTokenExpirationSeconds()).thenReturn(900L);

        AuthResponse response = authService.login(new LoginRequest(openidToken, devUserId));

        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        verify(oidcVerificationPort, never()).verifyOpenIdToken(any());
        verify(sessionManagementPort).updateRefreshToken(99L, refreshToken);
    }

    @Test
    void logout_shouldRevokeSession_whenValidRefreshToken() {
        String refreshToken = "valid_refresh";
        Session session = Session.builder().id(10L).userId(1L).build();
        when(sessionManagementPort.findActiveSession(refreshToken)).thenReturn(Optional.of(session));

        authService.logout(refreshToken);

        verify(sessionManagementPort).revokeSession(10L);
    }
}
