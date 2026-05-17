package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorLookupAdapterTest {

    @Mock
    private UserQueryPort userQueryPort;

    @InjectMocks
    private AuthorLookupAdapter authorLookupAdapter;

    @Test
    void getAuthorBasicInfo_shouldReturnNameFromDisplayName() {
        when(userQueryPort.getDisplayName(1L)).thenReturn("@user:example.org");

        AuthorBasicInfo result = authorLookupAdapter.getAuthorBasicInfo(1L);

        assertThat(result.name()).isEqualTo("@user:example.org");
    }

    @Test
    void getAuthorBasicInfo_shouldReturnUserIdAsString_whenDisplayNameIsNull() {
        when(userQueryPort.getDisplayName(1L)).thenReturn(null);

        AuthorBasicInfo result = authorLookupAdapter.getAuthorBasicInfo(1L);

        assertThat(result.name()).isEqualTo("1");
    }
}
