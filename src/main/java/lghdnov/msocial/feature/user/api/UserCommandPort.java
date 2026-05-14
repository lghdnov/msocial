package lghdnov.msocial.feature.user.api;

import lghdnov.msocial.feature.user.presentation.AvatarDTO;
import lghdnov.msocial.feature.user.presentation.ProfileUpdateRequest;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Порт изменения данных пользователя.
 *
 * @implNote Реализация ({@code UserService}) выполняет частичное обновление
 *           {@code PersonalInfo} с валидацией через {@code ProfileValidator},
 *           а также управляет аватарами и медиа-треками.
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

    /**
     * Загружает аватар пользователя.
     *
     * @param userId идентификатор локального пользователя
     * @param file   файл аватара
     * @return обновлённый профиль
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пользователь не найден
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файл некорректен
     */
    UserDTO uploadAvatar(Long userId, MultipartFile file);

    /**
     * Загружает любимый трек пользователя.
     *
     * @param userId идентификатор локального пользователя
     * @param file   файл трека
     * @return обновлённый профиль
     * @throws lghdnov.msocial.common.exceptions.NotFoundException    если пользователь не найден
     * @throws lghdnov.msocial.common.exceptions.ValidationException если файл некорректен
     */
    UserDTO uploadTrack(Long userId, MultipartFile file);
}
