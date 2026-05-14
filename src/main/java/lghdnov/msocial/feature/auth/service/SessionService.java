package lghdnov.msocial.feature.auth.service;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.auth.api.SessionManagementPort;
import lghdnov.msocial.feature.auth.entity.Session;
import lghdnov.msocial.feature.auth.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Сервис управления сессиями.
 *
 * <p>Реализует {@link SessionManagementPort}, храня данные в PostgreSQL
 * через {@link SessionRepository}.
 */
@Service
class SessionService implements SessionManagementPort {

    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 30;

    private final SessionRepository sessionRepository;

    SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional
    public Session createSession(Long userId) {
        Session session = Session.builder()
            .userId(userId)
            .createdAt(Instant.now())
            .refreshTokenExpiresAt(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, ChronoUnit.DAYS))
            .build();
        return sessionRepository.save(session);
    }

    @Override
    @Transactional
    public void revokeSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("SESSION_NOT_FOUND", "Сессия не найдена"));
        session.setRevokedAt(Instant.now());
        sessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Session> findActiveSession(String refreshToken) {
        return sessionRepository.findByRefreshToken(refreshToken)
            .filter(Session::isActive);
    }

    @Override
    @Transactional
    public void updateRefreshToken(Long sessionId, String refreshToken) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new NotFoundException("SESSION_NOT_FOUND", "Сессия не найдена"));
        session.setRefreshToken(refreshToken);
        sessionRepository.save(session);
    }
}
