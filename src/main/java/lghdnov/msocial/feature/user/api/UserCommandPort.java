package lghdnov.msocial.feature.user.api;

import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;

/**
 * Порт изменения данных пользователя.
 *
 * @implNote Реализация ({@code UserService}) выполняет частичное обновление
 *           {@code PersonalInfo} с валидацией через {@code ProfileValidator}.
 * @see lghdnov.msocial.feature.user.service.UserService
 */
public interface UserCommandPort {

    /**
     * Обновляет персональные данные пользователя.
     *
     * @param userId  идентификатор локального пользователя
     * @param request данные для обновления
     * @return обновлённый профиль
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пользователь не найден
     * @throws lghdnov.msocial.common.exceptions.ValidationException если данные не прошли валидацию
     */
    UserDTO updateProfile(Long userId, ProfileUpdateRequest request);
}
