package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.feature.comment.api.AuthorLookupPort;
import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.springframework.stereotype.Component;

/**
 * Адаптер поиска информации об авторе.
 *
 * <p>
 * Реализует {@link AuthorLookupPort}, делегируя вызовы в {@code UserQueryPort}
 * из модуля {@code feature::user}.
 */
@Component
class AuthorLookupAdapter implements AuthorLookupPort {

  private final UserQueryPort userQueryPort;

  AuthorLookupAdapter(UserQueryPort userQueryPort) {
    this.userQueryPort = userQueryPort;
  }

  @Override
  public AuthorBasicInfo getAuthorBasicInfo(Long userId) {
    UserDTO userDTO = userQueryPort.getProfile(userId);
    String name = userDTO.externalId() != null ? userDTO.externalId() : String.valueOf(userId);
    return new AuthorBasicInfo(name);
  }
}
