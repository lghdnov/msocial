package lghdnov.msocial.feature.comment.infrastructure;

import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.post.api.PostQueryPort;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostContextAdapterTest {

    @Mock
    private PostQueryPort postQueryPort;

    @InjectMocks
    private PostContextAdapter postContextAdapter;

    @Test
    void isPostVisible_shouldReturnTrue_whenPostExists() {
        when(postQueryPort.getPost(null, 1L))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Hello", List.of(), true, Instant.now()));

        assertThat(postContextAdapter.isPostVisible(1L)).isTrue();
    }

    @Test
    void isPostVisible_shouldReturnFalse_whenPostNotFound() {
        when(postQueryPort.getPost(null, 99L))
            .thenThrow(new NotFoundException("POST_NOT_FOUND", "Пост не найден"));

        assertThat(postContextAdapter.isPostVisible(99L)).isFalse();
    }

    @Test
    void allowsComments_shouldReturnTrue_whenPostVisible() {
        when(postQueryPort.getPost(null, 1L))
            .thenReturn(new PostDTO(1L, 1L, "@user:example.org", "Hello", List.of(), true, Instant.now()));

        assertThat(postContextAdapter.allowsComments(1L)).isTrue();
    }

    @Test
    void allowsComments_shouldReturnFalse_whenPostNotVisible() {
        when(postQueryPort.getPost(null, 99L))
            .thenThrow(new NotFoundException("POST_NOT_FOUND", "Пост не найден"));

        assertThat(postContextAdapter.allowsComments(99L)).isFalse();
    }
}
