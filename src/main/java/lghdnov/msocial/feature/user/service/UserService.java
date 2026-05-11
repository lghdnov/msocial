package lghdnov.msocial.feature.user.service;

import lghdnov.msocial.feature.auth.entity.MatrixUserInfo;
import lghdnov.msocial.feature.user.api.UserProvisioningPort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Заглушка сервиса пользователей.
 *
 * <p>Реализует {@link UserProvisioningPort} для интеграции с фичей {@code auth}.
 * В полноценной реализации должен работать с JPA-сущностью {@code User}.
 */
@Service
public class UserService implements UserProvisioningPort {

    private final Map<String, Long> matrixToLocalId = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Long findByIdOrCreate(String externalId, MatrixUserInfo userInfo) {
        return matrixToLocalId.computeIfAbsent(externalId, k -> idGenerator.getAndIncrement());
    }

    @Override
    public boolean isAccountActive(Long userId) {
        return userId != null && matrixToLocalId.containsValue(userId);
    }
}
