package lghdnov.msocial.feature.user.api;

import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.UserDTO;

import java.util.List;

/**
 * Порт чтения данных пользователя.
 *
 * @implNote Реализация ({@code UserService}) выполняет выборку через JPA-репозитории.
 * @see lghdnov.msocial.feature.user.service.UserService
 */
public interface UserQueryPort {

    /**
     * Возвращает профиль пользователя по локальному идентификатору.
     *
     * @param userId идентификатор локального пользователя
     * @return DTO профиля
     * @throws lghdnov.msocial.common.exceptions.NotFoundException если пользователь не найден
     */
    UserDTO getProfile(Long userId);

    /**
     * Возвращает историю аватаров пользователя.
     *
     * @param userId идентификатор локального пользователя
     * @return список аватаров, отсортированных по дате загрузки (новые первые)
     */
    List<AvatarDTO> getAvatarHistory(Long userId);
}
