package lghdnov.msocial.feature.comment.service;

import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.comment.api.AuthorLookupPort;
import lghdnov.msocial.feature.comment.api.PostContextPort;
import lghdnov.msocial.feature.comment.entity.Comment;
import lghdnov.msocial.feature.comment.entity.CommentStatus;
import lghdnov.msocial.feature.comment.presentation.AuthorBasicInfo;
import lghdnov.msocial.feature.comment.presentation.CommentDTO;
import lghdnov.msocial.feature.comment.presentation.CreateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.UpdateCommentRequest;
import lghdnov.msocial.feature.comment.presentation.mapper.CommentMapper;
import lghdnov.msocial.feature.comment.repository.CommentRepository;
import lghdnov.msocial.feature.comment.service.validator.CommentValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentValidator commentValidator;

    @Mock
    private PostContextPort postContextPort;

    @Mock
    private AuthorLookupPort authorLookupPort;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getByPostId_shouldReturnPage() {
        Long postId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        Comment comment = Comment.builder().id(1L).postId(postId).authorId(1L).authorName("@user:example.org").content("Hello").status(CommentStatus.PUBLISHED).build();
        CommentDTO dto = new CommentDTO(1L, postId, 1L, "@user:example.org", null, "Hello", CommentStatus.PUBLISHED, Instant.now(), null);

        when(commentRepository.findRootByPostIdAndStatusNot(postId, CommentStatus.DELETED, pageable))
            .thenReturn(new PageImpl<>(List.of(comment)));
        when(commentMapper.toDto(comment)).thenReturn(dto);

        var result = commentService.getByPostId(postId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Hello");
    }

    @Test
    void getById_shouldReturnDto() {
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).postId(1L).authorId(1L).authorName("@user:example.org").content("Hello").status(CommentStatus.PUBLISHED).build();
        CommentDTO dto = new CommentDTO(commentId, 1L, 1L, "@user:example.org", null, "Hello", CommentStatus.PUBLISHED, Instant.now(), null);

        when(commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)).thenReturn(Optional.of(comment));
        when(commentMapper.toDto(comment)).thenReturn(dto);

        CommentDTO result = commentService.getById(commentId);

        assertThat(result.id()).isEqualTo(commentId);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(commentRepository.findByIdAndStatusNot(99L, CommentStatus.DELETED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getById(99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Комментарий не найден");
    }

    @Test
    void getReplies_shouldReturnPage() {
        Long parentId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        Comment parent = Comment.builder().id(parentId).postId(1L).authorId(1L).authorName("@user:example.org").content("Parent").status(CommentStatus.PUBLISHED).build();
        Comment reply = Comment.builder().id(2L).postId(1L).authorId(2L).authorName("@reply:example.org").content("Reply").parentId(parentId).status(CommentStatus.PUBLISHED).build();
        CommentDTO dto = new CommentDTO(2L, 1L, 2L, "@reply:example.org", parentId, "Reply", CommentStatus.PUBLISHED, Instant.now(), null);

        when(commentRepository.findByIdAndStatusNot(parentId, CommentStatus.DELETED)).thenReturn(Optional.of(parent));
        when(commentRepository.findByParentIdAndStatusNot(parentId, CommentStatus.DELETED, pageable))
            .thenReturn(new PageImpl<>(List.of(reply)));
        when(commentMapper.toDto(reply)).thenReturn(dto);

        var result = commentService.getReplies(parentId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).parentId()).isEqualTo(parentId);
    }

    @Test
    void create_shouldCreateAndReturnDto() {
        Long userId = 1L;
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("Nice post!", null);
        AuthorBasicInfo authorInfo = new AuthorBasicInfo("@user:example.org");
        Comment comment = Comment.builder().postId(postId).authorId(userId).authorName("@user:example.org").content("Nice post!").build();
        Comment saved = Comment.builder().id(1L).postId(postId).authorId(userId).authorName("@user:example.org").content("Nice post!").status(CommentStatus.PUBLISHED).build();
        CommentDTO dto = new CommentDTO(1L, postId, userId, "@user:example.org", null, "Nice post!", CommentStatus.PUBLISHED, Instant.now(), null);

        when(postContextPort.isPostVisible(postId)).thenReturn(true);
        when(postContextPort.allowsComments(postId)).thenReturn(true);
        when(authorLookupPort.getAuthorBasicInfo(userId)).thenReturn(authorInfo);
        when(commentMapper.toEntity(request, postId, userId, "@user:example.org")).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(saved);
        when(commentMapper.toDto(saved)).thenReturn(dto);

        CommentDTO result = commentService.create(userId, postId, request);

        assertThat(result.content()).isEqualTo("Nice post!");
        verify(commentValidator).validateCreate(request);
    }

    @Test
    void create_shouldThrow_whenPostNotVisible() {
        Long userId = 1L;
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("Nice post!", null);

        when(postContextPort.isPostVisible(postId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.create(userId, postId, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пост не найден или недоступен");
    }

    @Test
    void create_shouldThrow_whenCommentsDisabled() {
        Long userId = 1L;
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("Nice post!", null);

        when(postContextPort.isPostVisible(postId)).thenReturn(true);
        when(postContextPort.allowsComments(postId)).thenReturn(false);

        assertThatThrownBy(() -> commentService.create(userId, postId, request))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Комментирование этого поста отключено");
    }

    @Test
    void create_shouldThrow_whenParentNotFound() {
        Long userId = 1L;
        Long postId = 1L;
        CreateCommentRequest request = new CreateCommentRequest("Nice post!", 99L);

        when(postContextPort.isPostVisible(postId)).thenReturn(true);
        when(postContextPort.allowsComments(postId)).thenReturn(true);
        when(commentRepository.findByIdAndStatusNot(99L, CommentStatus.DELETED)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.create(userId, postId, request))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Комментарий не найден");
    }

    @Test
    void update_shouldUpdateAndReturnDto() {
        Long userId = 1L;
        Long commentId = 1L;
        UpdateCommentRequest request = new UpdateCommentRequest("Updated");
        Comment comment = Comment.builder().id(commentId).postId(1L).authorId(userId).authorName("@user:example.org").content("Old").status(CommentStatus.PUBLISHED).build();
        Comment saved = Comment.builder().id(commentId).postId(1L).authorId(userId).authorName("@user:example.org").content("Updated").status(CommentStatus.PUBLISHED).build();
        CommentDTO dto = new CommentDTO(commentId, 1L, userId, "@user:example.org", null, "Updated", CommentStatus.PUBLISHED, Instant.now(), Instant.now());

        when(commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(saved);
        when(commentMapper.toDto(saved)).thenReturn(dto);

        CommentDTO result = commentService.update(userId, commentId, request);

        assertThat(result.content()).isEqualTo("Updated");
        verify(commentValidator).validateUpdate(request);
    }

    @Test
    void update_shouldThrow_whenNotAuthor() {
        Long userId = 2L;
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).postId(1L).authorId(1L).content("Old").status(CommentStatus.PUBLISHED).build();

        when(commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.update(userId, commentId, new UpdateCommentRequest("Updated")))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Вы не являетесь автором");
    }

    @Test
    void delete_shouldSoftDelete() {
        Long userId = 1L;
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).postId(1L).authorId(userId).content("Hello").status(CommentStatus.PUBLISHED).build();

        when(commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)).thenReturn(Optional.of(comment));

        commentService.delete(userId, commentId);

        assertThat(comment.getStatus()).isEqualTo(CommentStatus.DELETED);
        verify(commentRepository).save(comment);
    }

    @Test
    void delete_shouldThrow_whenNotAuthor() {
        Long userId = 2L;
        Long commentId = 1L;
        Comment comment = Comment.builder().id(commentId).postId(1L).authorId(1L).content("Hello").status(CommentStatus.PUBLISHED).build();

        when(commentRepository.findByIdAndStatusNot(commentId, CommentStatus.DELETED)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> commentService.delete(userId, commentId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Вы не являетесь автором");
    }
}
