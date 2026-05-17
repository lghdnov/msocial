package lghdnov.msocial.feature.comment.api;

import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;

/**
 * Порт для получения базовой информации об авторе комментария.
 *
 * <p>Определяется в {@code feature::comment} как consumer-driven contract
 * и реализуется в адаптере, который делегирует вызовы в {@code feature::user}.
 *
 * @implNote Реализация ({@code AuthorLookupAdapter}) вызывает {@code UserQueryPort}
 *           и извлекает отображаемое имя пользователя.
 * @see lghdnov.msocial.feature.comment.infrastructure.AuthorLookupAdapter
 */
public interface AuthorLookupPort {

    /**
     * Возвращает базовую информацию об авторе по его идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return базовая информация об авторе
     * @throws lghdnov.msocial.common.exceptions.NotFoundException если пользователь не найден
     */
    AuthorBasicInfo getAuthorBasicInfo(Long userId);
}
