package lghdnov.msocial.feature.post.service;

import lghdnov.msocial.common.exceptions.AccessDeniedException;
import lghdnov.msocial.common.exceptions.NotFoundException;
import lghdnov.msocial.feature.post.api.PostMediaStoragePort;
import lghdnov.msocial.feature.post.entity.Post;
import lghdnov.msocial.feature.post.entity.PostMedia;
import lghdnov.msocial.feature.post.presentation.CreatePostRequest;
import lghdnov.msocial.feature.post.presentation.PostDTO;
import lghdnov.msocial.feature.post.presentation.PostMediaDTO;
import lghdnov.msocial.feature.post.presentation.UpdatePostRequest;
import lghdnov.msocial.feature.post.presentation.mapper.PostMapper;
import lghdnov.msocial.feature.post.repository.PostMediaRepository;
import lghdnov.msocial.feature.post.repository.PostRepository;
import lghdnov.msocial.feature.post.service.validator.PostMediaValidator;
import lghdnov.msocial.feature.user.api.UserQueryPort;
import lghdnov.msocial.feature.user.presentation.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserQueryPort userQueryPort;

    @Mock
    private PostMediaStoragePort postMediaStoragePort;

    @Mock
    private PostMediaValidator postMediaValidator;

    @InjectMocks
    private PostService postService;

    @Test
    void getPost_shouldReturnDto_whenPostExistsAndPublished() {
        Long postId = 1L;
        Long userId = 1L;
        Post post = Post.builder().id(postId).authorId(userId).authorName("@user:example.org").content("Hello").published(true).build();
        List<PostMedia> media = List.of();
        PostDTO dto = new PostDTO(postId, userId, "@user:example.org", "Hello", List.of(), true, Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(postId)).thenReturn(media);
        when(postMapper.toDto(post, media)).thenReturn(dto);

        PostDTO result = postService.getPost(userId, postId);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getPost_shouldReturnDto_whenPostUnpublishedAndUserIsAuthor() {
        Long postId = 1L;
        Long userId = 1L;
        Post post = Post.builder().id(postId).authorId(userId).authorName("@user:example.org").content("Draft").published(false).build();
        List<PostMedia> media = List.of();
        PostDTO dto = new PostDTO(postId, userId, "@user:example.org", "Draft", List.of(), false, Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(postId)).thenReturn(media);
        when(postMapper.toDto(post, media)).thenReturn(dto);

        PostDTO result = postService.getPost(userId, postId);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getPost_shouldThrow_whenPostUnpublishedAndNotAuthor() {
        Long postId = 1L;
        Long userId = 2L;
        Post post = Post.builder().id(postId).authorId(1L).authorName("@user:example.org").content("Draft").published(false).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.getPost(userId, postId))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пост не найден");
    }

    @Test
    void getPost_shouldThrow_whenPostNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(1L, 99L))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Пост не найден");
    }

    @Test
    void getFeed_shouldReturnPage() {
        Long authorId = 1L;
        PageRequest pageable = PageRequest.of(0, 10);
        Post post = Post.builder().id(1L).authorId(authorId).authorName("@user:example.org").content("Hello").published(true).build();
        List<PostMedia> media = List.of();
        PostDTO dto = new PostDTO(1L, authorId, "@user:example.org", "Hello", List.of(), true, Instant.now());

        when(postRepository.findAllByAuthorId(authorId, pageable)).thenReturn(new PageImpl<>(List.of(post)));
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(1L)).thenReturn(media);
        when(postMapper.toDto(post, media)).thenReturn(dto);

        var result = postService.getFeed(authorId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Hello");
    }

    @Test
    void createPost_shouldCreateUnpublishedAndReturnDto() {
        Long authorId = 1L;
        CreatePostRequest request = new CreatePostRequest("New post");
        UserDTO userDTO = new UserDTO(1L, "@user:example.org", null, Instant.now());
        Post post = Post.builder().authorId(authorId).authorName("@user:example.org").content("New post").published(false).build();
        Post saved = Post.builder().id(1L).authorId(authorId).authorName("@user:example.org").content("New post").published(false).build();
        PostDTO dto = new PostDTO(1L, authorId, "@user:example.org", "New post", List.of(), false, Instant.now());

        when(userQueryPort.getProfile(authorId)).thenReturn(userDTO);
        when(postRepository.save(any(Post.class))).thenReturn(saved);
        when(postMapper.toDto(saved, List.of())).thenReturn(dto);

        PostDTO result = postService.createPost(authorId, request);

        assertThat(result.content()).isEqualTo("New post");
        assertThat(result.authorName()).isEqualTo("@user:example.org");
        assertThat(result.published()).isFalse();
    }

    @Test
    void updatePost_shouldUpdateAndReturnDto() {
        Long userId = 1L;
        Long postId = 1L;
        UpdatePostRequest request = new UpdatePostRequest("Updated");
        Post post = Post.builder().id(postId).authorId(userId).authorName("@user:example.org").content("Old").published(true).build();
        Post saved = Post.builder().id(postId).authorId(userId).authorName("@user:example.org").content("Updated").published(true).build();
        List<PostMedia> media = List.of();
        PostDTO dto = new PostDTO(postId, userId, "@user:example.org", "Updated", List.of(), true, Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(saved);
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(postId)).thenReturn(media);
        when(postMapper.toDto(saved, media)).thenReturn(dto);

        PostDTO result = postService.updatePost(userId, postId, request);

        assertThat(result.content()).isEqualTo("Updated");
    }

    @Test
    void updatePost_shouldThrow_whenNotAuthor() {
        Long userId = 2L;
        Long postId = 1L;
        Post post = Post.builder().id(postId).authorId(1L).published(true).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.updatePost(userId, postId, new UpdatePostRequest("Test")))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Вы не являетесь автором");
    }

    @Test
    void deletePost_shouldSoftDelete() {
        Long userId = 1L;
        Long postId = 1L;
        Post post = Post.builder().id(postId).authorId(userId).deleted(false).published(true).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(userId, postId);

        assertThat(post.getDeleted()).isTrue();
        verify(postRepository).save(post);
    }

    @Test
    void publishPost_shouldSetPublishedTrue() {
        Long userId = 1L;
        Long postId = 1L;
        Post post = Post.builder().id(postId).authorId(userId).content("Draft").published(false).build();
        Post saved = Post.builder().id(postId).authorId(userId).content("Draft").published(true).build();
        List<PostMedia> media = List.of();
        PostDTO dto = new PostDTO(postId, userId, "@user:example.org", "Draft", List.of(), true, Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(saved);
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(postId)).thenReturn(media);
        when(postMapper.toDto(saved, media)).thenReturn(dto);

        PostDTO result = postService.publishPost(userId, postId);

        assertThat(result.published()).isTrue();
        verify(postRepository).save(post);
    }

    @Test
    void publishPost_shouldThrow_whenNotAuthor() {
        Long userId = 2L;
        Long postId = 1L;
        Post post = Post.builder().id(postId).authorId(1L).published(false).build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        assertThatThrownBy(() -> postService.publishPost(userId, postId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("Вы не являетесь автором");
    }

    @Test
    void addPostMedia_shouldAddMediaAndReturnDto() {
        Long userId = 1L;
        Long postId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});
        Post post = Post.builder().id(postId).authorId(userId).published(true).build();
        PostMedia media = PostMedia.builder().postId(postId).url("/post-media/uuid.jpg").sortOrder(0).build();
        List<PostMedia> allMedia = List.of(media);
        PostDTO dto = new PostDTO(postId, userId, "@user:example.org", "Content", List.of(new PostMediaDTO(1L, "/post-media/uuid.jpg", 0)), true, Instant.now());

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findByPostIdOrderBySortOrderAsc(postId)).thenReturn(List.of(), allMedia);
        when(postMediaStoragePort.uploadPostMedia(file)).thenReturn("/post-media/uuid.jpg");
        when(postMapper.toDto(post, allMedia)).thenReturn(dto);

        PostDTO result = postService.addPostMedia(userId, postId, List.of(file));

        assertThat(result.media()).hasSize(1);
        verify(postMediaStoragePort).uploadPostMedia(file);
        verify(postMediaRepository).save(any(PostMedia.class));
    }

    @Test
    void deletePostMedia_shouldDeleteFileAndRecord() {
        Long userId = 1L;
        Long postId = 1L;
        Long mediaId = 1L;
        Post post = Post.builder().id(postId).authorId(userId).published(true).build();
        PostMedia media = PostMedia.builder().id(mediaId).postId(postId).url("/post-media/image.jpg").build();

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postMediaRepository.findById(mediaId)).thenReturn(Optional.of(media));

        postService.deletePostMedia(userId, postId, mediaId);

        verify(postMediaStoragePort).delete("/post-media/image.jpg");
        verify(postMediaRepository).delete(media);
    }
}
