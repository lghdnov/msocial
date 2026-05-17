package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.PersonalInfoDTO;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorLookupAdapterTest {

    @Mock
    private UserQueryPort userQueryPort;

    @InjectMocks
    private AuthorLookupAdapter authorLookupAdapter;

    @Test
    void getAuthorBasicInfo_shouldReturnNameFromExternalId() {
        UserDTO userDTO = new UserDTO(1L, "@user:example.org", new PersonalInfoDTO(null, null, null, null), Instant.now());
        when(userQueryPort.getProfile(1L)).thenReturn(userDTO);

        AuthorBasicInfo result = authorLookupAdapter.getAuthorBasicInfo(1L);

        assertThat(result.name()).isEqualTo("@user:example.org");
    }

    @Test
    void getAuthorBasicInfo_shouldReturnUserIdAsString_whenExternalIdIsNull() {
        UserDTO userDTO = new UserDTO(1L, null, new PersonalInfoDTO(null, null, null, null), Instant.now());
        when(userQueryPort.getProfile(1L)).thenReturn(userDTO);

        AuthorBasicInfo result = authorLookupAdapter.getAuthorBasicInfo(1L);

        assertThat(result.name()).isEqualTo("1");
    }
}
