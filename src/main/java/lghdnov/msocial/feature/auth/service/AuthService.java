package lghdnov.msocial.feature.auth.service;

import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.common.exceptions.ValidationException;
import lghdnov.msocial.feature.auth.api.AuthCommandPort;
import lghdnov.msocial.feature.auth.api.OidcVerificationPort;
import lghdnov.msocial.feature.auth.api.SessionManagementPort;
import lghdnov.msocial.feature.auth.api.TokenGenerationPort;
import lghdnov.msocial.feature.auth.entity.JwtClaims;
import lghdnov.msocial.feature.auth.entity.MatrixUserInfo;
import lghdnov.msocial.feature.auth.entity.Session;
import lghdnov.msocial.feature.auth.presentation.AuthResponse;
import lghdnov.msocial.feature.auth.presentation.LoginRequest;
import lghdnov.msocial.feature.auth.presentation.RefreshRequest;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Центральный сервис аутентификации.
 *
 * <p>Реализует {@link AuthCommandPort},
 * оркестрируя проверку OIDC, провижининг пользователя, генерацию JWT
 * и управление сессиями.
 */
@Service
class AuthService implements AuthCommandPort {

    private final OidcVerificationPort oidcVerificationPort;
    private final UserProvisioningPort userProvisioningPort;
    private final TokenGenerationPort tokenGenerationPort;
    private final SessionManagementPort sessionManagementPort;

    @Value("${auth.dev.skip-verify:false}")
    private boolean skipVerify;

    AuthService(
        OidcVerificationPort oidcVerificationPort,
        UserProvisioningPort userProvisioningPort,
        TokenGenerationPort tokenGenerationPort,
        SessionManagementPort sessionManagementPort
    ) {
        this.oidcVerificationPort = oidcVerificationPort;
        this.userProvisioningPort = userProvisioningPort;
        this.tokenGenerationPort = tokenGenerationPort;
        this.sessionManagementPort = sessionManagementPort;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        if (request.openidToken() == null || request.openidToken().isBlank()) {
            throw new ValidationException("OIDC_TOKEN_EMPTY", "OpenID токен обязателен");
        }

        MatrixUserInfo matrixUser;
        if (skipVerify && request.userId() != null && !request.userId().isBlank()) {
            matrixUser = new MatrixUserInfo(request.userId());
        } else {
            matrixUser = oidcVerificationPort.verifyOpenIdToken(request.openidToken());
        }
        Long userId = userProvisioningPort.findByIdOrCreate(matrixUser.sub());

        if (!userProvisioningPort.isAccountActive(userId)) {
            throw new AccessDeniedException("ACCOUNT_INACTIVE", "Аккаунт заблокирован или удалён");
        }

        Session session = sessionManagementPort.createSession(userId);
        String refreshToken = tokenGenerationPort.generateRefreshToken(session.getId());
        sessionManagementPort.updateRefreshToken(session.getId(), refreshToken);

        JwtClaims claims = new JwtClaims(
            String.valueOf(userId),
            Set.of("ROLE_USER"),
            session.getId()
        );
        String accessToken = tokenGenerationPort.generateAccessToken(userId, claims);

        return new AuthResponse(
            accessToken,
            refreshToken,
            tokenGenerationPort.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        if (request.refreshToken() == null || request.refreshToken().isBlank()) {
            throw new ValidationException("REFRESH_TOKEN_EMPTY", "Refresh-токен обязателен");
        }

        Session session = sessionManagementPort.findActiveSession(request.refreshToken())
            .orElseThrow(() -> new NotFoundException("SESSION_NOT_FOUND", "Сессия не найдена или истекла"));

        sessionManagementPort.revokeSession(session.getId());
        Session newSession = sessionManagementPort.createSession(session.getUserId());
        String newRefreshToken = tokenGenerationPort.generateRefreshToken(newSession.getId());
        sessionManagementPort.updateRefreshToken(newSession.getId(), newRefreshToken);

        JwtClaims claims = new JwtClaims(
            String.valueOf(newSession.getUserId()),
            Set.of("ROLE_USER"),
            newSession.getId()
        );
        String newAccessToken = tokenGenerationPort.generateAccessToken(newSession.getUserId(), claims);

        return new AuthResponse(
            newAccessToken,
            newRefreshToken,
            tokenGenerationPort.getAccessTokenExpirationSeconds()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ValidationException("REFRESH_TOKEN_EMPTY", "Refresh-токен обязателен");
        }

        Session session = sessionManagementPort.findActiveSession(refreshToken)
            .orElseThrow(() -> new NotFoundException("SESSION_NOT_FOUND", "Сессия не найдена"));
        sessionManagementPort.revokeSession(session.getId());
    }
}
