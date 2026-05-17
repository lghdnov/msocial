package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.feature.comment.api.AuthorLookupPort;
import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Адаптер поиска информации об авторе.
 *
 * <p>Реализует {@link AuthorLookupPort}, делегируя вызовы в {@code UserQueryPort}
 * из модуля {@code feature::user}.
 */
@Component
@RequiredArgsConstructor
class AuthorLookupAdapter implements AuthorLookupPort {

    private final UserQueryPort userQueryPort;

    @Override
    public AuthorBasicInfo getAuthorBasicInfo(Long userId) {
        String displayName = userQueryPort.getDisplayName(userId);
        String name = displayName != null ? displayName : String.valueOf(userId);
        return new AuthorBasicInfo(name);
    }
}
