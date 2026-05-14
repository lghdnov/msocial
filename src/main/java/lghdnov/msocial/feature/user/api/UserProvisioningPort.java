package lghdnov.msocial.feature.user.api;

/**
 * Порт провижининга пользователя для фичи аутентификации.
 *
 * <p>Реализуется в {@code feature::user} ({@code UserService}).
 * Фича {@code auth} не знает о внутренней структуре {@code User}.
 *
 * @implNote Сервис {@code UserService} проверяет существование пользователя
 *           по внешнему Matrix ID и при необходимости создаёт новую запись
 *           с базовым профилем.
 * @see lghdnov.msocial.feature.auth.service.AuthService
 */
public interface UserProvisioningPort {

    /**
     * Находит пользователя по внешнему Matrix ID или создаёт нового.
     *
     * @implNote Сервис {@code UserService} проверяет существование пользователя
     *           по {@code externalId} и при необходимости создаёт новую запись
     *           в таблице {@code users} с пустым {@code PersonalInfo}.
     * @param externalId Matrix User ID (например, {@code @user:example.org})
     * @return идентификатор локального пользователя
     * @throws lghdnov.msocial.common.exceptions.ValidationException если {@code externalId} пустой
     */
    Long findByIdOrCreate(String externalId);

    /**
     * Проверяет, активен ли аккаунт пользователя.
     *
     * @implNote Считается активным, если запись {@code User} существует в БД.
     * @param userId идентификатор локального пользователя
     * @return {@code true}, если аккаунт не заблокирован и не удалён
     */
    boolean isAccountActive(Long userId);
}
